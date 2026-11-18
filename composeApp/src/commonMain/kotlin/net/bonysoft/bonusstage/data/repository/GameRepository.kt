package net.bonysoft.bonusstage.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.cache.RefreshPolicy
import net.bonysoft.bonusstage.data.cache.Refresher
import net.bonysoft.bonusstage.db.Achievement_unlock
import net.bonysoft.bonusstage.db.BonusStageDatabase
import net.bonysoft.bonusstage.db.Game_progress
import net.bonysoft.bonusstage.domain.AchievementType
import net.bonysoft.bonusstage.domain.Award
import net.bonysoft.bonusstage.domain.GameDetail
import net.bonysoft.bonusstage.raapi.RaMedia
import kotlin.time.Instant
import net.bonysoft.bonusstage.db.Achievement as AchievementRow
import net.bonysoft.bonusstage.domain.Achievement as AchievementModel

/** A game's details and achievement set with the user's unlocks (API_GetGameInfoAndUserProgress). */
class GameRepository(
    private val database: BonusStageDatabase,
    private val accounts: AccountRepository,
    private val refresher: Refresher,
    private val policy: RefreshPolicy,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val queries get() = database.cacheQueries

    /** Emits null until the game is cached. */
    fun observeGame(gameId: Long): Flow<GameDetail?> {
        val ulid = accounts.requireUlid()
        return combine(
            queries.game(gameId).asFlow().mapToOneOrNull(dispatcher),
            queries.achievementsForGame(ulid, gameId).asFlow().mapToList(dispatcher),
            queries.progressForGame(ulid, gameId).asFlow().mapToOneOrNull(dispatcher),
        ) { game, achievements, progress ->
            if (game == null || refresher.fetchedAt(resource(ulid, gameId)) == null) return@combine null
            val players = game.num_players?.takeIf { it > 0 }
            GameDetail(
                id = game.id,
                title = game.title,
                consoleName = game.console_name,
                iconUrl = RaMedia.image(game.image_icon),
                boxArtUrl = game.image_box_art?.let(RaMedia::image),
                publisher = game.publisher?.takeIf { it.isNotBlank() },
                developer = game.developer?.takeIf { it.isNotBlank() },
                genre = game.genre?.takeIf { it.isNotBlank() },
                released = game.released?.takeIf { it.isNotBlank() },
                players = players?.toInt(),
                award = Award.from(progress?.highest_award_kind),
                achievements = achievements.map {
                    AchievementModel(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        points = it.points.toInt(),
                        trueRatio = it.true_ratio.toInt(),
                        badgeUrl = RaMedia.badge(it.badge_name),
                        lockedBadgeUrl = RaMedia.badge(it.badge_name, locked = true),
                        type = AchievementType.from(it.type),
                        unlockedAt = it.earned_at?.let(Instant::fromEpochMilliseconds),
                        unlockedHardcoreAt = it.earned_hardcore_at?.let(Instant::fromEpochMilliseconds),
                        unlockRate = players?.let { total -> (it.num_awarded.toFloat() / total).coerceIn(0f, 1f) },
                    )
                },
            )
        }
    }

    suspend fun refresh(gameId: Long, force: Boolean = false): Boolean {
        val ulid = accounts.requireUlid()
        return refresher.refresh(resource(ulid, gameId), policy.gameDetail, force) {
            val info = accounts.client().getGameInfoAndUserProgress(gameId, ulid)
            database.transaction {
                queries.insertGameIfMissing(info.id, info.title, info.consoleId.toLong(), info.consoleName, info.imageIcon)
                queries.updateGame(
                    title = info.title, consoleId = info.consoleId.toLong(), consoleName = info.consoleName,
                    imageIcon = info.imageIcon, imageBoxArt = info.imageBoxArt, publisher = info.publisher,
                    developer = info.developer, genre = info.genre, released = info.released,
                    numPlayers = info.numDistinctPlayers.toLong(), id = info.id,
                )
                queries.deleteAchievementsForGame(gameId)
                queries.deleteUnlocksForGame(ulid, gameId)
                info.achievements.values.forEach { a ->
                    queries.insertAchievement(
                        AchievementRow(
                            id = a.id, game_id = gameId, title = a.title, description = a.description,
                            points = a.points.toLong(), true_ratio = a.trueRatio.toLong(), badge_name = a.badgeName,
                            display_order = a.displayOrder.toLong(), type = a.type,
                            num_awarded = a.numAwarded.toLong(), num_awarded_hardcore = a.numAwardedHardcore.toLong(),
                        ),
                    )
                    if (a.dateEarned != null || a.dateEarnedHardcore != null) {
                        queries.insertUnlock(
                            Achievement_unlock(ulid, a.id, gameId, a.dateEarned.toEpochMillis(), a.dateEarnedHardcore.toEpochMillis()),
                        )
                    }
                }
                val earned = info.achievements.values.mapNotNull { (it.dateEarned ?: it.dateEarnedHardcore).toEpochMillis() }
                queries.upsertProgress(
                    Game_progress(
                        ulid = ulid,
                        game_id = gameId,
                        max_possible = info.numAchievements.toLong(),
                        num_awarded = info.numAwardedToUser.toLong(),
                        num_awarded_hardcore = info.numAwardedToUserHardcore.toLong(),
                        most_recent_awarded = earned.maxOrNull(),
                        highest_award_kind = info.highestAwardKind,
                        highest_award_date = info.highestAwardDate.toEpochMillis(),
                    ),
                )
            }
        }
    }

    private fun resource(ulid: String, gameId: Long) = "game:$ulid:$gameId"
}
