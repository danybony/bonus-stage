package net.bonysoft.bonusstage.domain

import kotlin.time.Instant

/** The highest award a user holds for a game, as RA ranks them. */
enum class Award(val apiValue: String) {
    BeatenSoftcore("beaten-softcore"),
    BeatenHardcore("beaten-hardcore"),
    Completed("completed"),
    Mastered("mastered"),
    ;

    companion object {
        fun from(value: String?): Award? = entries.firstOrNull { it.apiValue == value }
    }
}

data class Profile(
    val username: String,
    val avatarUrl: String,
    val memberSince: Instant?,
    val motto: String?,
    val richPresence: String?,
    val hardcorePoints: Long,
    val softcorePoints: Long,
    val truePoints: Long,
    val rank: Long?,
    val totalRanked: Long?,
    val isOnline: Boolean,
    val recentlyPlayed: List<RecentlyPlayedGame>,
    val awards: AwardCounts?,
)

data class RecentlyPlayedGame(
    val gameId: Long,
    val title: String,
    val consoleName: String,
    val iconUrl: String,
    val lastPlayed: Instant?,
)

data class AwardCounts(
    val mastered: Int,
    val completed: Int,
    val beatenHardcore: Int,
    val beatenSoftcore: Int,
    val events: Int,
)

data class LibraryGame(
    val id: Long,
    val title: String,
    val consoleId: Int,
    val consoleName: String,
    val iconUrl: String,
    val achievementsTotal: Int,
    val unlocked: Int,
    val unlockedHardcore: Int,
    val lastUnlock: Instant?,
    val award: Award?,
) {
    val progress: Float get() = if (achievementsTotal == 0) 0f else unlocked.toFloat() / achievementsTotal
}

data class GameDetail(
    val id: Long,
    val title: String,
    val consoleName: String,
    val iconUrl: String,
    val boxArtUrl: String?,
    val publisher: String?,
    val developer: String?,
    val genre: String?,
    val released: String?,
    val players: Int?,
    val award: Award?,
    val achievements: List<Achievement>,
) {
    val unlocked: Int get() = achievements.count { it.isUnlocked }
    val unlockedHardcore: Int get() = achievements.count { it.unlockedHardcoreAt != null }
    val totalPoints: Int get() = achievements.sumOf { it.points }
    val earnedPoints: Int get() = achievements.filter { it.isUnlocked }.sumOf { it.points }
}

enum class AchievementType(val apiValue: String) {
    Progression("progression"),
    WinCondition("win_condition"),
    Missable("missable"),
    ;

    companion object {
        fun from(value: String?): AchievementType? = entries.firstOrNull { it.apiValue == value }
    }
}

data class Achievement(
    val id: Long,
    val title: String,
    val description: String,
    val points: Int,
    val trueRatio: Int,
    val badgeUrl: String,
    val lockedBadgeUrl: String,
    val type: AchievementType?,
    val unlockedAt: Instant?,
    val unlockedHardcoreAt: Instant?,
    /** Share of the game's players with this achievement, 0..1, when known. */
    val unlockRate: Float?,
) {
    val isUnlocked: Boolean get() = unlockedAt != null || unlockedHardcoreAt != null
}

data class RecentUnlock(
    val achievementId: Long,
    val title: String,
    val description: String,
    val points: Int,
    val badgeUrl: String,
    val hardcore: Boolean,
    val unlockedAt: Instant,
    val gameId: Long,
    val gameTitle: String,
    val gameIconUrl: String,
    val consoleName: String,
)

/** Where a wishlisted game's achievement set stands. */
sealed interface SetStatus {
    data class Published(val achievements: Int, val points: Int) : SetStatus
    data class InDevelopment(val developer: String, val isRevision: Boolean, val expectedBy: Instant?) : SetStatus
    data object NoSet : SetStatus
}

data class WishlistGame(
    val gameId: Long,
    val title: String,
    val iconUrl: String,
    val consoleName: String,
    val status: SetStatus,
)

data class FriendUnlock(
    val friendUlid: String,
    val friendName: String,
    val friendAvatarUrl: String,
    val achievementId: Long,
    val title: String,
    val description: String,
    val points: Int,
    val badgeUrl: String,
    val hardcore: Boolean,
    val unlockedAt: Instant,
    val gameId: Long,
    val gameTitle: String,
    val gameIconUrl: String,
)
