package net.bonysoft.bonusstage.raapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A page of a paginated list endpoint (`c` count, `o` offset). */
@Serializable
data class Page<T>(
    @SerialName("Count") val count: Int,
    @SerialName("Total") val total: Int,
    @SerialName("Results") val results: List<T>,
)

/** One entry of `API_GetUserCompletionProgress`. */
@Serializable
data class GameProgress(
    @SerialName("GameID") val gameId: Long,
    @SerialName("Title") val title: String,
    @SerialName("ImageIcon") val imageIcon: String,
    @SerialName("ConsoleID") val consoleId: Int,
    @SerialName("ConsoleName") val consoleName: String,
    @SerialName("MaxPossible") val maxPossible: Int,
    @SerialName("NumAwarded") val numAwarded: Int,
    @SerialName("NumAwardedHardcore") val numAwardedHardcore: Int,
    @SerialName("MostRecentAwardedDate") val mostRecentAwardedDate: String? = null,
    /** One of `beaten-softcore`, `beaten-hardcore`, `completed`, `mastered`, or null. */
    @SerialName("HighestAwardKind") val highestAwardKind: String? = null,
    @SerialName("HighestAwardDate") val highestAwardDate: String? = null,
)

/** One entry of `API_GetUserWantToPlayList`. */
@Serializable
data class WantToPlayGame(
    @SerialName("ID") val gameId: Long,
    @SerialName("Title") val title: String,
    @SerialName("ImageIcon") val imageIcon: String,
    @SerialName("ConsoleID") val consoleId: Int,
    @SerialName("ConsoleName") val consoleName: String,
    @SerialName("PointsTotal") val pointsTotal: Int,
    @SerialName("AchievementsPublished") val achievementsPublished: Int,
)
