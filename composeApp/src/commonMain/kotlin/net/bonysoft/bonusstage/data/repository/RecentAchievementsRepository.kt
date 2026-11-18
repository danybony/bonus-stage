package net.bonysoft.bonusstage.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.cache.RefreshPolicy
import net.bonysoft.bonusstage.data.cache.Refresher
import net.bonysoft.bonusstage.db.BonusStageDatabase
import net.bonysoft.bonusstage.db.Recent_unlock
import net.bonysoft.bonusstage.domain.RecentUnlock
import net.bonysoft.bonusstage.raapi.RaMedia
import kotlin.time.Instant

/** Recently unlocked achievements (API_GetUserRecentAchievements). Old entries are kept as history. */
class RecentAchievementsRepository(
    private val database: BonusStageDatabase,
    private val accounts: AccountRepository,
    private val refresher: Refresher,
    private val policy: RefreshPolicy,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val queries get() = database.cacheQueries

    fun observeRecent(limit: Int = 100): Flow<List<RecentUnlock>> =
        queries.recentUnlocks(accounts.requireUlid(), limit.toLong()).asFlow().mapToList(dispatcher).map { rows ->
            rows.map {
                RecentUnlock(
                    achievementId = it.achievement_id,
                    title = it.title,
                    description = it.description,
                    points = it.points.toInt(),
                    badgeUrl = RaMedia.badge(it.badge_name),
                    hardcore = it.hardcore == 1L,
                    unlockedAt = Instant.fromEpochMilliseconds(it.unlocked_at),
                    gameId = it.game_id,
                    gameTitle = it.game_title,
                    gameIconUrl = RaMedia.image(it.game_icon),
                    consoleName = it.console_name,
                )
            }
        }

    fun hasBeenFetched(): Boolean = refresher.fetchedAt("recent:${accounts.requireUlid()}") != null

    /**
     * Fetches the last [WINDOW_MINUTES] of unlocks. Returns the unlocks that were not cached before,
     * for notifications and widgets; empty when the refresh was skipped.
     */
    suspend fun refresh(force: Boolean = false): List<RecentUnlock> {
        val ulid = accounts.requireUlid()
        val newOnes = mutableListOf<RecentUnlock>()
        refresher.refresh("recent:$ulid", policy.recentAchievements, force) {
            val recent = accounts.client().getUserRecentAchievements(ulid, WINDOW_MINUTES)
            database.transaction {
                val known = queries.recentUnlocks(ulid, Long.MAX_VALUE).executeAsList().map { it.achievement_id }.toSet()
                recent.forEach { a ->
                    val unlockedAt = a.date.toEpochMillis() ?: return@forEach
                    val row = Recent_unlock(
                        ulid = ulid, achievement_id = a.achievementId, game_id = a.gameId, unlocked_at = unlockedAt,
                        hardcore = a.hardcoreMode.toLong(), title = a.title, description = a.description,
                        badge_name = a.badgeName, points = a.points.toLong(), game_title = a.gameTitle,
                        game_icon = a.gameIcon, console_name = a.consoleName,
                    )
                    queries.insertRecentUnlock(row)
                    if (a.achievementId !in known) {
                        newOnes += RecentUnlock(
                            a.achievementId, a.title, a.description, a.points, RaMedia.badge(a.badgeName), a.hardcoreMode,
                            Instant.fromEpochMilliseconds(unlockedAt), a.gameId, a.gameTitle, RaMedia.image(a.gameIcon), a.consoleName,
                        )
                    }
                }
            }
        }
        return newOnes
    }

    companion object {
        /** Two weeks of unlocks per request. */
        const val WINDOW_MINUTES = 14 * 24 * 60
    }
}
