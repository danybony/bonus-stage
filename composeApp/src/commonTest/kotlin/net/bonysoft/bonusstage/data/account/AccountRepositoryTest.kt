package net.bonysoft.bonusstage.data.account

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import net.bonysoft.bonusstage.data.demo.DemoData
import net.bonysoft.bonusstage.raapi.RaApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccountRepositoryTest {

    private class FakeStore(var value: String? = null) : CredentialStore {
        override suspend fun load() = value
        override suspend fun save(value: String) { this.value = value }
        override suspend fun clear() { value = null }
    }

    private val profileJson = """
        {"User":"RealName","ULID":"01ULID","UserPic":"/UserPic/RealName.png","MemberSince":"2020-01-01 00:00:00",
         "TotalPoints":10,"TotalSoftcorePoints":0,"TotalTruePoints":20,"ID":5}
    """

    private fun TestScope.repository(
        store: FakeStore = FakeStore(),
        cleared: MutableList<String> = mutableListOf(),
        status: HttpStatusCode = HttpStatusCode.OK,
    ) = AccountRepository(
        store = store,
        httpClient = HttpClient(MockEngine { if (status == HttpStatusCode.OK) respond(profileJson) else respondError(status) }),
        scope = backgroundScope,
        cleaners = { listOf(UserDataCleaner { cleared += "cache" }) },
    )

    @Test
    fun startsSignedOutWithEmptyStore() = runTest {
        val repository = repository()
        testScheduler.runCurrent()

        assertEquals(Session.SignedOut, repository.session.value)
    }

    @Test
    fun signInStoresCanonicalNameAndUlid() = runTest {
        val store = FakeStore()
        val repository = repository(store)

        repository.signIn("  realname ", " key ")

        val account = repository.currentAccount!!
        assertEquals("RealName", account.username)
        assertEquals("01ULID", account.ulid)
        assertEquals("key", account.webApiKey)
        assertTrue(store.value!!.contains("01ULID"))
    }

    @Test
    fun storedAccountIsRestored() = runTest {
        val store = FakeStore()
        repository(store).signIn("realname", "key")

        val restored = repository(store)
        testScheduler.runCurrent()

        assertEquals("01ULID", restored.currentAccount?.ulid)
    }

    @Test
    fun rejectedKeyKeepsSignedOut() = runTest {
        val store = FakeStore()
        val repository = repository(store, status = HttpStatusCode.Unauthorized)
        testScheduler.runCurrent()

        assertFailsWith<RaApiException.Unauthorized> { repository.signIn("realname", "bad") }
        assertNull(store.value)
        assertEquals(Session.SignedOut, repository.session.value)
    }

    @Test
    fun signOutClearsStoreAndCaches() = runTest {
        val store = FakeStore()
        val cleared = mutableListOf<String>()
        val repository = repository(store, cleared)
        repository.signIn("realname", "key")

        repository.signOut()

        assertNull(store.value)
        assertEquals(listOf("cache"), cleared)
        assertEquals(Session.SignedOut, repository.session.value)
    }

    @Test
    fun demoAccountUsesBundledData() = runTest {
        val repository = repository(status = HttpStatusCode.Unauthorized)

        repository.signInDemo()

        assertTrue(repository.currentAccount!!.isDemo)
        assertEquals(DemoData.USERNAME, repository.client().getUserProfile(DemoData.USERNAME).user)
    }

    @Test
    fun corruptedStoreStartsSignedOut() = runTest {
        val repository = repository(FakeStore("not json"))
        testScheduler.runCurrent()

        assertFalse(repository.session.value is Session.SignedIn)
    }
}
