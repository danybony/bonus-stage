package net.bonysoft.bonusstage.raapi

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.CancellationException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import net.bonysoft.bonusstage.raapi.model.AchievementOfTheWeek
import net.bonysoft.bonusstage.raapi.model.ActiveClaim
import net.bonysoft.bonusstage.raapi.model.Console
import net.bonysoft.bonusstage.raapi.model.FollowedUser
import net.bonysoft.bonusstage.raapi.model.Follower
import net.bonysoft.bonusstage.raapi.model.GameInfoAndUserProgress
import net.bonysoft.bonusstage.raapi.model.GameListEntry
import net.bonysoft.bonusstage.raapi.model.GameProgress
import net.bonysoft.bonusstage.raapi.model.Page
import net.bonysoft.bonusstage.raapi.model.RecentAchievement
import net.bonysoft.bonusstage.raapi.model.UserAwards
import net.bonysoft.bonusstage.raapi.model.UserProfile
import net.bonysoft.bonusstage.raapi.model.UserSummary
import net.bonysoft.bonusstage.raapi.model.WantToPlayGame

/**
 * Client for the RetroAchievements Web API (https://api-docs.retroachievements.org/).
 *
 * Every call is authenticated with the credentials returned by [credentials] at call time, so the
 * app can sign in and out without rebuilding the client. `user` parameters accept a username or a
 * ULID; prefer the ULID once known, usernames can change.
 */
class RetroAchievementsClient(
    private val credentials: () -> RaCredentials?,
    httpClient: HttpClient = HttpClient(),
    private val json: Json = RaJson.app,
    private val throttler: RequestThrottler = RequestThrottler(),
    private val baseUrl: String = BASE_URL,
) {
    private val http = httpClient.config {
        expectSuccess = false
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
        }
    }

    suspend fun getUserProfile(user: String): UserProfile =
        call("API_GetUserProfile", UserProfile.serializer(), "u" to user)

    suspend fun getUserSummary(user: String, recentGames: Int = 0, recentAchievements: Int = 0): UserSummary =
        call("API_GetUserSummary", UserSummary.serializer(), "u" to user, "g" to recentGames, "a" to recentAchievements)

    /** [count] is capped at 500 by the API. */
    suspend fun getUserCompletionProgress(user: String, count: Int = 500, offset: Int = 0): Page<GameProgress> =
        call(
            "API_GetUserCompletionProgress",
            Page.serializer(GameProgress.serializer()),
            "u" to user, "c" to count, "o" to offset,
        )

    suspend fun getGameInfoAndUserProgress(gameId: Long, user: String): GameInfoAndUserProgress =
        call("API_GetGameInfoAndUserProgress", GameInfoAndUserProgress.serializer(), "g" to gameId, "u" to user)

    /** Achievements unlocked in the last [minutes] (default 60 on the API side). */
    suspend fun getUserRecentAchievements(user: String, minutes: Int = 60): List<RecentAchievement> =
        call(
            "API_GetUserRecentAchievements",
            ListSerializer(RecentAchievement.serializer()),
            "u" to user, "m" to minutes,
        )

    /** Unlocks between two moments, as epoch seconds. Same entries as [getUserRecentAchievements]. */
    suspend fun getAchievementsEarnedBetween(user: String, fromEpochSeconds: Long, toEpochSeconds: Long): List<RecentAchievement> =
        call(
            "API_GetAchievementsEarnedBetween",
            ListSerializer(RecentAchievement.serializer()),
            "u" to user, "f" to fromEpochSeconds, "t" to toEpochSeconds,
        )

    suspend fun getUserWantToPlayList(user: String, count: Int = 500, offset: Int = 0): Page<WantToPlayGame> =
        call(
            "API_GetUserWantToPlayList",
            Page.serializer(WantToPlayGame.serializer()),
            "u" to user, "c" to count, "o" to offset,
        )

    /** The people the owner of the web API key follows (no user parameter: it is always the key's owner). */
    suspend fun getUsersIFollow(count: Int = 500, offset: Int = 0): Page<FollowedUser> =
        call("API_GetUsersIFollow", Page.serializer(FollowedUser.serializer()), "c" to count, "o" to offset)

    /** The people who follow the owner of the web API key. */
    suspend fun getUsersFollowingMe(count: Int = 500, offset: Int = 0): Page<Follower> =
        call("API_GetUsersFollowingMe", Page.serializer(Follower.serializer()), "c" to count, "o" to offset)

    suspend fun getActiveClaims(): List<ActiveClaim> =
        call("API_GetActiveClaims", ListSerializer(ActiveClaim.serializer()))

    suspend fun getUserAwards(user: String): UserAwards =
        call("API_GetUserAwards", UserAwards.serializer(), "u" to user)

    suspend fun getConsoleIds(activeOnly: Boolean = true, gameSystemsOnly: Boolean = true): List<Console> =
        call(
            "API_GetConsoleIDs",
            ListSerializer(Console.serializer()),
            "a" to activeOnly.toFlag(), "g" to gameSystemsOnly.toFlag(),
        )

    /** The console's games; [withHashes] adds the ROM hashes each game supports (for ROM scans). */
    suspend fun getGameList(consoleId: Int, withAchievementsOnly: Boolean = true, withHashes: Boolean = false): List<GameListEntry> =
        call(
            "API_GetGameList",
            ListSerializer(GameListEntry.serializer()),
            "i" to consoleId, "f" to withAchievementsOnly.toFlag(), "h" to withHashes.toFlag(),
        )

    suspend fun getAchievementOfTheWeek(): AchievementOfTheWeek =
        call("API_GetAchievementOfTheWeek", AchievementOfTheWeek.serializer())

    /** Checks the credentials with the cheapest call, returning the canonical profile. */
    suspend fun verify(credentials: RaCredentials): UserProfile =
        call("API_GetUserProfile", UserProfile.serializer(), "u" to credentials.username, override = credentials)

    private suspend fun <T> call(
        endpoint: String,
        deserializer: DeserializationStrategy<T>,
        vararg params: Pair<String, Any>,
        override: RaCredentials? = null,
    ): T {
        val body = fetch(endpoint, *params, override = override)
        return try {
            json.decodeFromString(deserializer, body)
        } catch (e: SerializationException) {
            throw RaApiException.UnexpectedResponse(e)
        } catch (e: IllegalArgumentException) {
            throw RaApiException.UnexpectedResponse(e)
        }
    }

    /** Returns the raw response body. Used directly by the contract tests to snapshot the schema. */
    internal suspend fun fetch(
        endpoint: String,
        vararg params: Pair<String, Any>,
        override: RaCredentials? = null,
    ): String {
        val auth = override ?: credentials() ?: throw RaApiException.Unauthorized()
        val response = throttler.throttle {
            try {
                http.get("$baseUrl$endpoint.php") {
                    parameter("y", auth.webApiKey)
                    params.forEach { (key, value) -> parameter(key, value) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                throw RaApiException.Network(e)
            }
        }
        checkStatus(response)
        return response.bodyAsText()
    }

    private fun checkStatus(response: HttpResponse) {
        when (response.status) {
            HttpStatusCode.OK -> Unit
            HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> throw RaApiException.Unauthorized()
            HttpStatusCode.NotFound, HttpStatusCode.UnprocessableEntity -> throw RaApiException.NotFound()
            HttpStatusCode.TooManyRequests ->
                throw RaApiException.RateLimited(response.headers[HttpHeaders.RetryAfter]?.toLongOrNull())
            else -> throw RaApiException.Http(response.status.value)
        }
    }

    private fun Boolean.toFlag() = if (this) 1 else 0

    companion object {
        const val BASE_URL = "https://retroachievements.org/API/"
    }
}
