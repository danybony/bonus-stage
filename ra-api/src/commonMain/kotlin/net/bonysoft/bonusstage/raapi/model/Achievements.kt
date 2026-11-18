package net.bonysoft.bonusstage.raapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.bonysoft.bonusstage.raapi.internal.FlexibleBooleanSerializer

/** One entry of `API_GetUserRecentAchievements`. */
@Serializable
data class RecentAchievement(
    @SerialName("Date") val date: String,
    @Serializable(with = FlexibleBooleanSerializer::class)
    @SerialName("HardcoreMode") val hardcoreMode: Boolean,
    @SerialName("AchievementID") val achievementId: Long,
    @SerialName("Title") val title: String,
    @SerialName("Description") val description: String,
    @SerialName("BadgeName") val badgeName: String,
    @SerialName("Points") val points: Int,
    @SerialName("TrueRatio") val trueRatio: Int,
    @SerialName("Type") val type: String? = null,
    @SerialName("GameTitle") val gameTitle: String,
    @SerialName("GameIcon") val gameIcon: String,
    @SerialName("GameID") val gameId: Long,
    @SerialName("ConsoleName") val consoleName: String,
)

/** `API_GetAchievementOfTheWeek` */
@Serializable
data class AchievementOfTheWeek(
    @SerialName("Achievement") val achievement: Achievement,
    @SerialName("Console") val console: Ref,
    @SerialName("Game") val game: Ref,
    @SerialName("StartAt") val startAt: String,
    @SerialName("TotalPlayers") val totalPlayers: Int,
    @SerialName("UnlocksCount") val unlocksCount: Int,
    @SerialName("UnlocksHardcoreCount") val unlocksHardcoreCount: Int,
) {
    @Serializable
    data class Achievement(
        @SerialName("ID") val id: Long,
        @SerialName("Title") val title: String,
        @SerialName("Description") val description: String,
        @SerialName("Points") val points: Int,
        @SerialName("TrueRatio") val trueRatio: Int? = null,
        @SerialName("Type") val type: String? = null,
        @SerialName("Author") val author: String? = null,
        @SerialName("BadgeName") val badgeName: String? = null,
    )

    @Serializable
    data class Ref(
        @SerialName("ID") val id: Long,
        @SerialName("Title") val title: String,
    )
}

/** `API_GetUserAwards` */
@Serializable
data class UserAwards(
    @SerialName("TotalAwardsCount") val totalAwardsCount: Int,
    @SerialName("HiddenAwardsCount") val hiddenAwardsCount: Int,
    @SerialName("MasteryAwardsCount") val masteryAwardsCount: Int,
    @SerialName("CompletionAwardsCount") val completionAwardsCount: Int,
    @SerialName("BeatenHardcoreAwardsCount") val beatenHardcoreAwardsCount: Int,
    @SerialName("BeatenSoftcoreAwardsCount") val beatenSoftcoreAwardsCount: Int,
    @SerialName("EventAwardsCount") val eventAwardsCount: Int,
    @SerialName("SiteAwardsCount") val siteAwardsCount: Int,
    @SerialName("VisibleUserAwards") val visibleUserAwards: List<UserAward>,
)

@Serializable
data class UserAward(
    @SerialName("AwardedAt") val awardedAt: String,
    /** `Mastery/Completion`, `Game Beaten`, `Event`, `Achievement Points Yield`, ... */
    @SerialName("AwardType") val awardType: String,
    /** The game ID for game awards. */
    @SerialName("AwardData") val awardData: Long? = null,
    /** 1 for hardcore, 0 for softcore. */
    @SerialName("AwardDataExtra") val awardDataExtra: Int? = null,
    @SerialName("DisplayOrder") val displayOrder: Int = 0,
    @SerialName("Title") val title: String? = null,
    @SerialName("ConsoleID") val consoleId: Int? = null,
    @SerialName("ConsoleName") val consoleName: String? = null,
    @SerialName("ImageIcon") val imageIcon: String? = null,
)

/** One entry of `API_GetActiveClaims`: a developer is working on an achievement set. */
@Serializable
data class ActiveClaim(
    @SerialName("ID") val id: Long,
    @SerialName("User") val user: String,
    @SerialName("GameID") val gameId: Long,
    @SerialName("GameTitle") val gameTitle: String,
    @SerialName("GameIcon") val gameIcon: String,
    @SerialName("ConsoleID") val consoleId: Int,
    @SerialName("ConsoleName") val consoleName: String,
    /** 0 primary, 1 collaboration. */
    @SerialName("ClaimType") val claimType: Int,
    /** 0 new set, 1 revision. */
    @SerialName("SetType") val setType: Int,
    @SerialName("Status") val status: Int,
    @SerialName("Created") val created: String,
    @SerialName("DoneTime") val doneTime: String,
    @SerialName("MinutesLeft") val minutesLeft: Long,
)
