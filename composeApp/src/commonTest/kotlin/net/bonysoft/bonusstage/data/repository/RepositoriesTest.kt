package net.bonysoft.bonusstage.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.account.CredentialStore
import net.bonysoft.bonusstage.data.cache.RefreshPolicy
import net.bonysoft.bonusstage.data.cache.Refresher
import net.bonysoft.bonusstage.data.createTestDatabase
import net.bonysoft.bonusstage.data.demo.demoHttpClient
import net.bonysoft.bonusstage.domain.Award
import net.bonysoft.bonusstage.domain.SetStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** The repositories against an in-memory database, fed by the demo API. */
class RepositoriesTest {

    private class FakeClock(var now: Instant = Instant.parse("2026-11-18T12:00:00Z")) : Clock {
        override fun now() = now
    }

    private val database = createTestDatabase()
    private val clock = FakeClock()
    private val policy = RefreshPolicy()
    private val refresher = Refresher(database, clock)
    private var apiCalls = 0

    private suspend fun TestScope.signedInDemo(): AccountRepository {
        val accounts = AccountRepository(
            store = object : CredentialStore {
                override suspend fun load(): String? = null
                override suspend fun save(value: String) = Unit
                override suspend fun clear() = Unit
            },
            httpClient = HttpClient(MockEngine { respondError(HttpStatusCode.Unauthorized) }),
            scope = backgroundScope,
            cleaners = { listOf(CacheCleaner(database)) },
            demoClient = demoHttpClient().config {
                install(io.ktor.client.plugins.api.createClientPlugin("count") { onRequest { _, _ -> apiCalls++ } })
            },
        )
        accounts.signInDemo()
        return accounts
    }

    @Test
    fun libraryIsCachedAndRefreshedOnlyWhenStale() = runTest {
        val accounts = signedInDemo()
        val library = LibraryRepository(database, accounts, refresher, policy, Dispatchers.Unconfined)
        assertTrue(library.observeLibrary().first().isEmpty())
        assertFalse(library.hasBeenFetched())

        assertTrue(library.refresh())
        val games = library.observeLibrary().first()
        assertEquals(6, games.size)
        assertEquals(Award.Mastered, games.first { it.title == "Crazy Kong" }.award)
        assertEquals(1, apiCalls)

        clock.now += 5.minutes
        assertFalse(library.refresh(), "fresh data is not fetched again")
        assertTrue(library.refresh(force = true), "pull-to-refresh always fetches")
        clock.now += policy.library
        assertTrue(library.refresh(), "stale data is fetched")
        assertEquals(3, apiCalls)
    }

    @Test
    fun gameDetailJoinsUnlocks() = runTest {
        val accounts = signedInDemo()
        val games = GameRepository(database, accounts, refresher, policy, Dispatchers.Unconfined)
        assertNull(games.observeGame(1).first())

        games.refresh(1)

        val sonic = assertNotNull(games.observeGame(1).first())
        assertEquals(10, sonic.achievements.size)
        assertEquals(7, sonic.unlocked)
        assertEquals("Sega", sonic.publisher)
        assertTrue(sonic.achievements.first().isUnlocked)
        assertEquals("That Was Easy", sonic.achievements.first().title, "achievements keep the set's display order")
    }

    @Test
    fun profileCombinesSummaryAndAwards() = runTest {
        val accounts = signedInDemo()
        val profiles = ProfileRepository(database, accounts, refresher, policy, Dispatchers.Unconfined)

        profiles.refresh()

        val profile = assertNotNull(profiles.observeProfile().first())
        assertEquals("BonusDemo", profile.username)
        assertEquals(4, profile.recentlyPlayed.size)
        assertEquals(1, profile.awards?.mastered)
        assertTrue(profile.isOnline)
    }

    @Test
    fun wishlistStatusComesFromSetsAndClaims() = runTest {
        val accounts = signedInDemo()
        val wishlist = WishlistRepository(database, accounts, refresher, policy, Dispatchers.Unconfined)

        wishlist.refresh()

        val byTitle = wishlist.observeWishlist().first().associateBy { it.title }
        assertIs<SetStatus.Published>(byTitle.getValue("Mega Man").status)
        val claimed = assertIs<SetStatus.InDevelopment>(byTitle.getValue("~Homebrew~ No Place To Hide").status)
        assertEquals("DemoDev", claimed.developer)
        assertEquals(SetStatus.NoSet, byTitle.getValue("~Hack~ Plumber For All Seasons, A").status)
    }

    @Test
    fun recentRefreshReportsOnlyNewUnlocks() = runTest {
        val accounts = signedInDemo()
        val recent = RecentAchievementsRepository(database, accounts, refresher, policy, Dispatchers.Unconfined)

        val first = recent.refresh()
        val second = recent.refresh(force = true)

        assertTrue(first.isNotEmpty())
        assertTrue(second.isEmpty())
        assertEquals(first.size, recent.observeRecent().first().size)
    }

    @Test
    fun signOutClearsTheCache() = runTest {
        val accounts = signedInDemo()
        val library = LibraryRepository(database, accounts, refresher, policy, Dispatchers.Unconfined)
        library.refresh()

        accounts.signOut()
        accounts.signInDemo()

        assertTrue(library.observeLibrary().first().isEmpty())
        assertFalse(library.hasBeenFetched())
    }

    @Test
    fun friendsAreFetchedInCappedRotation() = runTest {
        val accounts = signedInDemo()
        val friends = FriendsRepository(database, accounts, refresher, policy.copy(friendsPerRefresh = 2), clock, Dispatchers.Unconfined)

        assertEquals(2, friends.refresh(), "first refresh: capped at two of the three friends")
        assertEquals(1, friends.refresh(), "second refresh: only the friend not fetched yet")
        assertEquals(0, friends.refresh(), "everyone is fresh")
        clock.now += policy.friendUnlocks
        assertEquals(2, friends.refresh(), "stale again, still capped")

        val feed = friends.observeFeed().first()
        assertEquals(setOf("PixelPilgrim", "CartridgeKid", "SoftcoreSam"), feed.map { it.friendName }.toSet())
        assertTrue(feed.zipWithNext().all { (a, b) -> a.unlockedAt >= b.unlockedAt })
    }
}
