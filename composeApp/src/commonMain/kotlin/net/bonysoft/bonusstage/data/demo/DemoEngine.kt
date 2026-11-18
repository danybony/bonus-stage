package net.bonysoft.bonusstage.data.demo

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import net.bonysoft.bonusstage.raapi.RaJson
import net.bonysoft.bonusstage.raapi.model.AchievementOfTheWeek
import net.bonysoft.bonusstage.raapi.model.ActiveClaim
import net.bonysoft.bonusstage.raapi.model.Console
import net.bonysoft.bonusstage.raapi.model.GameInfoAndUserProgress
import net.bonysoft.bonusstage.raapi.model.GameListEntry
import net.bonysoft.bonusstage.raapi.model.GameProgress
import net.bonysoft.bonusstage.raapi.model.Page
import net.bonysoft.bonusstage.raapi.model.RecentAchievement
import net.bonysoft.bonusstage.raapi.model.UserAwards
import net.bonysoft.bonusstage.raapi.model.UserProfile
import net.bonysoft.bonusstage.raapi.model.UserSummary
import net.bonysoft.bonusstage.raapi.model.WantToPlayGame

/** An HTTP client that answers RA API requests from [DemoData], without touching the network. */
fun demoHttpClient(): HttpClient = HttpClient(
    MockEngine { request ->
        val endpoint = request.url.encodedPath.substringAfterLast('/').removeSuffix(".php")
        val params = request.url.parameters
        fun <T> ok(serializer: KSerializer<T>, value: T) = respond(
            content = RaJson.app.encodeToString(serializer, value),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
        when (endpoint) {
            "API_GetUserProfile" -> ok(UserProfile.serializer(), DemoData.profile())
            "API_GetUserSummary" -> ok(UserSummary.serializer(), DemoData.summary())
            "API_GetUserCompletionProgress" -> ok(Page.serializer(GameProgress.serializer()), DemoData.completionProgress())
            "API_GetGameInfoAndUserProgress" -> DemoData.gameInfo(params["g"]?.toLongOrNull() ?: -1)
                ?.let { ok(GameInfoAndUserProgress.serializer(), it) }
                ?: respondError(HttpStatusCode.NotFound)
            "API_GetUserRecentAchievements" -> ok(
                ListSerializer(RecentAchievement.serializer()),
                DemoData.recentAchievements(params["m"]?.toIntOrNull() ?: 60),
            )
            "API_GetUserWantToPlayList" -> ok(Page.serializer(WantToPlayGame.serializer()), DemoData.wantToPlay())
            "API_GetActiveClaims" -> ok(ListSerializer(ActiveClaim.serializer()), DemoData.activeClaims())
            "API_GetUserAwards" -> ok(UserAwards.serializer(), DemoData.awards())
            "API_GetConsoleIDs" -> ok(ListSerializer(Console.serializer()), DemoData.consoles())
            "API_GetGameList" -> ok(ListSerializer(GameListEntry.serializer()), emptyList())
            "API_GetAchievementOfTheWeek" -> ok(AchievementOfTheWeek.serializer(), DemoData.achievementOfTheWeek())
            else -> respondError(HttpStatusCode.NotFound)
        }
    },
)
