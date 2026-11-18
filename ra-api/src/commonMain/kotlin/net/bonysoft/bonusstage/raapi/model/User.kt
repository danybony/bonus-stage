package net.bonysoft.bonusstage.raapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `API_GetUserProfile` */
@Serializable
data class UserProfile(
    @SerialName("User") val user: String,
    @SerialName("ULID") val ulid: String,
    @SerialName("UserPic") val userPic: String,
    @SerialName("MemberSince") val memberSince: String,
    @SerialName("RichPresenceMsg") val richPresenceMsg: String? = null,
    @SerialName("LastGameID") val lastGameId: Long? = null,
    @SerialName("TotalPoints") val totalPoints: Long,
    @SerialName("TotalSoftcorePoints") val totalSoftcorePoints: Long,
    @SerialName("TotalTruePoints") val totalTruePoints: Long,
    @SerialName("Motto") val motto: String? = null,
    @SerialName("ID") val id: Long,
)

/** `API_GetUserSummary`, only the fields the app uses. */
@Serializable
data class UserSummary(
    @SerialName("User") val user: String,
    @SerialName("ULID") val ulid: String,
    @SerialName("UserPic") val userPic: String,
    @SerialName("MemberSince") val memberSince: String,
    @SerialName("RichPresenceMsg") val richPresenceMsg: String? = null,
    @SerialName("LastGameID") val lastGameId: Long? = null,
    @SerialName("TotalPoints") val totalPoints: Long,
    @SerialName("TotalSoftcorePoints") val totalSoftcorePoints: Long,
    @SerialName("TotalTruePoints") val totalTruePoints: Long,
    @SerialName("Motto") val motto: String? = null,
    /** Null for users without a rank (for example, untracked or too few points). */
    @SerialName("Rank") val rank: Long? = null,
    @SerialName("TotalRanked") val totalRanked: Long? = null,
    @SerialName("Status") val status: String? = null,
    @SerialName("RecentlyPlayed") val recentlyPlayed: List<RecentlyPlayedGame> = emptyList(),
)

@Serializable
data class RecentlyPlayedGame(
    @SerialName("GameID") val gameId: Long,
    @SerialName("ConsoleID") val consoleId: Int,
    @SerialName("ConsoleName") val consoleName: String,
    @SerialName("Title") val title: String,
    @SerialName("ImageIcon") val imageIcon: String,
    @SerialName("LastPlayed") val lastPlayed: String,
    @SerialName("AchievementsTotal") val achievementsTotal: Int = 0,
)

/** One entry of `API_GetUsersIFollow`: someone the key's owner follows. */
@Serializable
data class FollowedUser(
    @SerialName("User") val user: String,
    @SerialName("ULID") val ulid: String,
    @SerialName("Points") val points: Long,
    @SerialName("PointsSoftcore") val pointsSoftcore: Long = 0,
    @Serializable(with = net.bonysoft.bonusstage.raapi.internal.FlexibleBooleanSerializer::class)
    @SerialName("IsFollowingMe") val isFollowingMe: Boolean = false,
)

/** One entry of `API_GetUsersFollowingMe`: someone who follows the key's owner. */
@Serializable
data class Follower(
    @SerialName("User") val user: String,
    @SerialName("ULID") val ulid: String,
    @SerialName("Points") val points: Long,
    @SerialName("PointsSoftcore") val pointsSoftcore: Long = 0,
    @Serializable(with = net.bonysoft.bonusstage.raapi.internal.FlexibleBooleanSerializer::class)
    @SerialName("AmIFollowing") val amIFollowing: Boolean = false,
)
