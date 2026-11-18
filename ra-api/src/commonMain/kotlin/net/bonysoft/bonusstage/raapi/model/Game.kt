package net.bonysoft.bonusstage.raapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.bonysoft.bonusstage.raapi.internal.FlexibleBooleanSerializer
import net.bonysoft.bonusstage.raapi.internal.PhpMapSerializer

/** `API_GetGameInfoAndUserProgress` */
@Serializable
data class GameInfoAndUserProgress(
    @SerialName("ID") val id: Long,
    @SerialName("Title") val title: String,
    @SerialName("ConsoleID") val consoleId: Int,
    @SerialName("ConsoleName") val consoleName: String,
    @SerialName("ImageIcon") val imageIcon: String,
    @SerialName("ImageTitle") val imageTitle: String? = null,
    @SerialName("ImageIngame") val imageIngame: String? = null,
    @SerialName("ImageBoxArt") val imageBoxArt: String? = null,
    @SerialName("Publisher") val publisher: String? = null,
    @SerialName("Developer") val developer: String? = null,
    @SerialName("Genre") val genre: String? = null,
    @SerialName("Released") val released: String? = null,
    @SerialName("NumDistinctPlayers") val numDistinctPlayers: Int,
    @SerialName("NumAchievements") val numAchievements: Int,
    @Serializable(with = AchievementMapSerializer::class)
    @SerialName("Achievements") val achievements: Map<String, GameAchievement>,
    @SerialName("NumAwardedToUser") val numAwardedToUser: Int,
    @SerialName("NumAwardedToUserHardcore") val numAwardedToUserHardcore: Int,
    @SerialName("UserTotalPlaytime") val userTotalPlaytime: Long? = null,
    @SerialName("HighestAwardKind") val highestAwardKind: String? = null,
    @SerialName("HighestAwardDate") val highestAwardDate: String? = null,
)

@Serializable
data class GameAchievement(
    @SerialName("ID") val id: Long,
    @SerialName("NumAwarded") val numAwarded: Int,
    @SerialName("NumAwardedHardcore") val numAwardedHardcore: Int,
    @SerialName("Title") val title: String,
    @SerialName("Description") val description: String,
    @SerialName("Points") val points: Int,
    @SerialName("TrueRatio") val trueRatio: Int,
    @SerialName("Author") val author: String? = null,
    @SerialName("BadgeName") val badgeName: String,
    @SerialName("DisplayOrder") val displayOrder: Int,
    /** `progression`, `win_condition`, `missable` or null. */
    @SerialName("type") val type: String? = null,
    @SerialName("DateEarned") val dateEarned: String? = null,
    @SerialName("DateEarnedHardcore") val dateEarnedHardcore: String? = null,
)

internal object AchievementMapSerializer : kotlinx.serialization.KSerializer<Map<String, GameAchievement>> by
    PhpMapSerializer(GameAchievement.serializer())

/** One entry of `API_GetGameList`. */
@Serializable
data class GameListEntry(
    @SerialName("ID") val id: Long,
    @SerialName("Title") val title: String,
    @SerialName("ConsoleID") val consoleId: Int,
    @SerialName("ConsoleName") val consoleName: String,
    @SerialName("ImageIcon") val imageIcon: String,
    @SerialName("NumAchievements") val numAchievements: Int,
    @SerialName("NumLeaderboards") val numLeaderboards: Int = 0,
    @SerialName("Points") val points: Int,
    @SerialName("DateModified") val dateModified: String? = null,
    @SerialName("Hashes") val hashes: List<String> = emptyList(),
)

/** One entry of `API_GetConsoleIDs`. */
@Serializable
data class Console(
    @SerialName("ID") val id: Int,
    @SerialName("Name") val name: String,
    @SerialName("IconURL") val iconUrl: String,
    @Serializable(with = FlexibleBooleanSerializer::class)
    @SerialName("Active") val active: Boolean,
    @Serializable(with = FlexibleBooleanSerializer::class)
    @SerialName("IsGameSystem") val isGameSystem: Boolean,
)
