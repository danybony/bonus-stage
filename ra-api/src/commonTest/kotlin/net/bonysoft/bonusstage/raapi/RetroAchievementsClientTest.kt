package net.bonysoft.bonusstage.raapi

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration

class RetroAchievementsClientTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun client(
        status: HttpStatusCode = HttpStatusCode.OK,
        headers: Map<String, String> = emptyMap(),
        body: String,
    ) = client { respond(body, status, headersOf(*headers.map { it.key to listOf(it.value) }.toTypedArray())) }

    private fun client(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        RetroAchievementsClient(
            credentials = { RaCredentials("tester", "secret-key") },
            httpClient = HttpClient(MockEngine { request ->
                requests += request
                handler(request)
            }),
            throttler = RequestThrottler(minInterval = Duration.ZERO),
        )

    private fun lastParams() = requests.last().url.parameters

    @Test
    fun userProfileSendsKeyAndUser() = runTest {
        val profile = client(body = Fixtures.USER_PROFILE).getUserProfile("MaxMilyin")

        assertEquals("MaxMilyin", profile.user)
        assertEquals("00003EMFWR7XB8SDPEHB3K56ZQ", profile.ulid)
        assertEquals(399_597, profile.totalPoints)
        assertEquals("/API/API_GetUserProfile.php", requests.last().url.encodedPath)
        assertEquals("secret-key", lastParams()["y"])
        assertEquals("MaxMilyin", lastParams()["u"])
    }

    @Test
    fun userSummaryToleratesEmptyPhpMaps() = runTest {
        val summary = client(body = Fixtures.USER_SUMMARY).getUserSummary("xelnia", recentGames = 3)

        assertEquals(4616, summary.rank)
        assertEquals("Crazy Kong", summary.recentlyPlayed.single().title)
        assertEquals("3", lastParams()["g"])
    }

    @Test
    fun completionProgressIsPaged() = runTest {
        val page = client(body = Fixtures.USER_COMPLETION_PROGRESS).getUserCompletionProgress("u", count = 50, offset = 100)

        assertEquals(2, page.total)
        assertEquals("beaten-hardcore", page.results[0].highestAwardKind)
        assertNull(page.results[1].highestAwardKind)
        assertEquals("50", lastParams()["c"])
        assertEquals("100", lastParams()["o"])
    }

    @Test
    fun gameInfoParsesAchievementMap() = runTest {
        val game = client(body = Fixtures.GAME_INFO_AND_USER_PROGRESS).getGameInfoAndUserProgress(1, "u")

        assertEquals(2, game.achievements.size)
        val first = game.achievements.getValue("9")
        assertEquals("progression", first.type)
        assertEquals("2016-03-12 17:47:29", first.dateEarnedHardcore)
        assertNull(game.achievements.getValue("10").dateEarned)
        assertEquals("1", lastParams()["g"])
    }

    @Test
    fun gameWithoutAchievementsHasEmptyMap() = runTest {
        val game = client(body = Fixtures.GAME_WITHOUT_ACHIEVEMENTS).getGameInfoAndUserProgress(30000, "u")

        assertTrue(game.achievements.isEmpty())
    }

    @Test
    fun recentAchievementsAcceptAnyBooleanEncoding() = runTest {
        val recent = client(body = Fixtures.USER_RECENT_ACHIEVEMENTS).getUserRecentAchievements("u", minutes = 1440)

        assertTrue(recent[0].hardcoreMode)
        assertFalse(recent[1].hardcoreMode)
        assertEquals("1440", lastParams()["m"])
    }

    @Test
    fun otherEndpointsDecode() = runTest {
        assertEquals(50, client(body = Fixtures.USER_WANT_TO_PLAY_LIST).getUserWantToPlayList("u").results.single().achievementsPublished)
        assertEquals(-41266, client(body = Fixtures.ACTIVE_CLAIMS).getActiveClaims().single().minutesLeft)
        assertEquals(1448, client(body = Fixtures.USER_AWARDS).getUserAwards("u").visibleUserAwards.single().awardData)
        val consoles = client(body = Fixtures.CONSOLE_IDS).getConsoleIds()
        assertTrue(consoles[0].active)
        assertFalse(consoles[1].isGameSystem)
        assertEquals("1", lastParams()["a"])
        assertEquals(2, client(body = Fixtures.GAME_LIST).getGameList(1).single().hashes.size)
        assertEquals("1", lastParams()["i"])
        assertEquals(268, client(body = Fixtures.ACHIEVEMENT_OF_THE_WEEK).getAchievementOfTheWeek().unlocksHardcoreCount)
    }

    @Test
    fun usersIFollowHasNoUserParameter() = runTest {
        val page = client(body = Fixtures.USERS_I_FOLLOW).getUsersIFollow(count = 50)

        assertEquals(listOf("zuliman92", "Scott"), page.results.map { it.user })
        assertTrue(page.results[0].isFollowingMe)
        assertNull(lastParams()["u"])
        assertEquals("50", lastParams()["c"])
    }

    @Test
    fun unauthorizedResponseMapsToUnauthorized() = runTest {
        assertFailsWith<RaApiException.Unauthorized> {
            client(HttpStatusCode.Unauthorized, body = """{"message":"Unauthenticated."}""").getUserProfile("u")
        }
    }

    @Test
    fun missingCredentialsFailWithoutCallingTheApi() = runTest {
        val client = RetroAchievementsClient(credentials = { null }, httpClient = HttpClient(MockEngine { error("no call") }))

        assertFailsWith<RaApiException.Unauthorized> { client.getUserProfile("u") }
    }

    @Test
    fun rateLimitCarriesRetryAfter() = runTest {
        val error = assertFailsWith<RaApiException.RateLimited> {
            client(HttpStatusCode.TooManyRequests, mapOf(HttpHeaders.RetryAfter to "30"), body = "").getUserProfile("u")
        }
        assertEquals(30, error.retryAfterSeconds)
    }

    @Test
    fun unexpectedShapeIsReported() = runTest {
        assertFailsWith<RaApiException.UnexpectedResponse> {
            client(body = """{"User": "x"}""").getUserProfile("u")
        }
    }

    @Test
    fun serverErrorsKeepTheStatus() = runTest {
        val error = assertFailsWith<RaApiException.Http> {
            client(HttpStatusCode.BadGateway, body = "").getConsoleIds()
        }
        assertEquals(502, error.status)
    }

    @Test
    fun credentialsAreNeverPrinted() {
        assertFalse("secret-key" in RaCredentials("tester", "secret-key").toString())
    }
}
