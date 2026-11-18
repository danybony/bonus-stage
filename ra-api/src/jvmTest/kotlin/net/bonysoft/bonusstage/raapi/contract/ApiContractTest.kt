package net.bonysoft.bonusstage.raapi.contract

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import net.bonysoft.bonusstage.raapi.RaCredentials
import net.bonysoft.bonusstage.raapi.RaJson
import net.bonysoft.bonusstage.raapi.RetroAchievementsClient
import net.bonysoft.bonusstage.raapi.model.AchievementOfTheWeek
import net.bonysoft.bonusstage.raapi.model.ActiveClaim
import net.bonysoft.bonusstage.raapi.model.Console
import net.bonysoft.bonusstage.raapi.model.FollowedUser
import net.bonysoft.bonusstage.raapi.model.GameInfoAndUserProgress
import net.bonysoft.bonusstage.raapi.model.GameListEntry
import net.bonysoft.bonusstage.raapi.model.GameProgress
import net.bonysoft.bonusstage.raapi.model.Page
import net.bonysoft.bonusstage.raapi.model.RecentAchievement
import net.bonysoft.bonusstage.raapi.model.UserAwards
import net.bonysoft.bonusstage.raapi.model.UserProfile
import net.bonysoft.bonusstage.raapi.model.UserSummary
import net.bonysoft.bonusstage.raapi.model.WantToPlayGame
import java.io.File
import kotlin.test.Test
import kotlin.test.fail

/**
 * Daily contract check against the live API (ADR 0003). Runs only through `:ra-api:contractTest`,
 * with `RA_USERNAME` and `RA_API_KEY` set to a dedicated account.
 *
 * For every endpoint the app uses it decodes the live response with [RaJson.strict] and compares
 * its schema with the snapshot in `src/jvmTest/resources/contract/`. Removed or retyped fields fail;
 * new fields are reported as warnings. Set `RA_UPDATE_SNAPSHOTS=true` to rewrite the snapshots.
 */
class ApiContractTest {

    private class Check<T>(
        val name: String,
        val endpoint: String,
        val deserializer: DeserializationStrategy<T>,
        val params: (user: String) -> Array<Pair<String, Any>>,
    )

    private val checks = listOf(
        Check("user-profile", "API_GetUserProfile", UserProfile.serializer()) { arrayOf("u" to it) },
        Check("user-summary", "API_GetUserSummary", UserSummary.serializer()) { arrayOf("u" to it, "g" to 3, "a" to 5) },
        Check("user-completion-progress", "API_GetUserCompletionProgress", Page.serializer(GameProgress.serializer())) {
            arrayOf("u" to it, "c" to 20)
        },
        Check("game-info-and-user-progress", "API_GetGameInfoAndUserProgress", GameInfoAndUserProgress.serializer()) {
            arrayOf("u" to it, "g" to REFERENCE_GAME_ID)
        },
        Check("user-recent-achievements", "API_GetUserRecentAchievements", ListSerializer(RecentAchievement.serializer())) {
            arrayOf("u" to it, "m" to 60 * 24 * 30)
        },
        Check("achievements-earned-between", "API_GetAchievementsEarnedBetween", ListSerializer(RecentAchievement.serializer())) {
            val now = System.currentTimeMillis() / 1000
            arrayOf("u" to it, "f" to now - 30L * 24 * 3600, "t" to now)
        },
        Check("user-want-to-play-list", "API_GetUserWantToPlayList", Page.serializer(WantToPlayGame.serializer())) {
            arrayOf("u" to it, "c" to 20)
        },
        Check("users-i-follow", "API_GetUsersIFollow", Page.serializer(FollowedUser.serializer())) { arrayOf("c" to 20) },
        Check("active-claims", "API_GetActiveClaims", ListSerializer(ActiveClaim.serializer())) { emptyArray() },
        Check("user-awards", "API_GetUserAwards", UserAwards.serializer()) { arrayOf("u" to it) },
        Check("console-ids", "API_GetConsoleIDs", ListSerializer(Console.serializer())) { arrayOf("a" to 1, "g" to 1) },
        Check("game-list", "API_GetGameList", ListSerializer(GameListEntry.serializer())) {
            arrayOf("i" to REFERENCE_CONSOLE_ID, "f" to 1, "h" to 1)
        },
        Check("achievement-of-the-week", "API_GetAchievementOfTheWeek", AchievementOfTheWeek.serializer()) { emptyArray() },
    )

    @Test
    fun liveResponsesMatchModelsAndSnapshots() = runBlocking {
        val username = System.getenv("RA_USERNAME")
        val apiKey = System.getenv("RA_API_KEY")
        if (username.isNullOrBlank() || apiKey.isNullOrBlank()) {
            println("RA_USERNAME or RA_API_KEY not set: skipping the API contract check")
            return@runBlocking
        }
        val updateSnapshots = System.getenv("RA_UPDATE_SNAPSHOTS") == "true"
        val client = RetroAchievementsClient(credentials = { RaCredentials(username, apiKey) })
        val failures = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        for (check in checks) {
            val body = try {
                client.fetch(check.endpoint, *check.params(username))
            } catch (e: Exception) {
                failures += "${check.name}: request failed: ${e.message}"
                continue
            }
            try {
                RaJson.strict.decodeFromString(check.deserializer, body)
            } catch (e: Exception) {
                failures += "${check.name}: does not decode with the strict models: ${e.message}"
            }

            val live = JsonSchema.of(body)
            val snapshotFile = File(SNAPSHOT_DIR, "${check.name}.schema")
            File(GENERATED_DIR, "${check.name}.schema").apply { parentFile.mkdirs() }.writeText(live.render())
            when {
                updateSnapshots -> snapshotFile.apply { parentFile.mkdirs() }.writeText(live.render())
                !snapshotFile.exists() -> warnings += "${check.name}: no committed snapshot, see $GENERATED_DIR"
                else -> {
                    val diff = live.diff(JsonSchema.parse(snapshotFile.readText()))
                    if (diff.isBreaking) failures += "${check.name}: schema changed\n${diff.describe()}"
                    else if (diff.added.isNotEmpty()) warnings += "${check.name}: new fields\n${diff.describe()}"
                }
            }
        }

        val report = buildString {
            appendLine("# RetroAchievements API contract check")
            appendLine()
            if (failures.isEmpty()) appendLine("All ${checks.size} endpoints match the models.")
            failures.forEach { appendLine("## Failure\n```\n$it\n```") }
            warnings.forEach { appendLine("## Warning\n```\n$it\n```") }
        }
        File(REPORT_FILE).apply { parentFile.mkdirs() }.writeText(report)
        println(report)
        if (failures.isNotEmpty()) fail(failures.joinToString("\n\n"))
    }

    private companion object {
        /** Sonic the Hedgehog (Mega Drive): a stable set with every achievement field filled. */
        const val REFERENCE_GAME_ID = 1L
        const val REFERENCE_CONSOLE_ID = 1

        const val SNAPSHOT_DIR = "src/jvmTest/resources/contract"
        const val GENERATED_DIR = "build/contract/generated"
        const val REPORT_FILE = "build/contract/report.md"
    }
}
