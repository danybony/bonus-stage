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
import net.bonysoft.bonusstage.db.Game_progress
import net.bonysoft.bonusstage.domain.Award
import net.bonysoft.bonusstage.domain.LibraryGame
import net.bonysoft.bonusstage.raapi.RaMedia
import kotlin.time.Instant

/** The games the user has played, with their progress (API_GetUserCompletionProgress). */
class LibraryRepository(
    private val database: BonusStageDatabase,
    private val accounts: AccountRepository,
    private val refresher: Refresher,
    private val policy: RefreshPolicy,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val queries get() = database.cacheQueries

    fun observeLibrary(): Flow<List<LibraryGame>> =
        queries.library(accounts.requireUlid()).asFlow().mapToList(dispatcher).map { rows ->
            rows.map {
                LibraryGame(
                    id = it.id,
                    title = it.title,
                    consoleId = it.console_id.toInt(),
                    consoleName = it.console_name,
                    iconUrl = RaMedia.image(it.image_icon),
                    achievementsTotal = it.max_possible.toInt(),
                    unlocked = it.num_awarded.toInt(),
                    unlockedHardcore = it.num_awarded_hardcore.toInt(),
                    lastUnlock = it.most_recent_awarded?.let(Instant::fromEpochMilliseconds),
                    award = Award.from(it.highest_award_kind),
                )
            }
        }

    /** True when the library has been fetched at least once, so an empty list really is empty. */
    fun hasBeenFetched(): Boolean = refresher.fetchedAt(resource()) != null

    suspend fun refresh(force: Boolean = false): Boolean {
        val ulid = accounts.requireUlid()
        return refresher.refresh(resource(), policy.library, force) {
            val client = accounts.client()
            val games = fetchAllPages { count, offset -> client.getUserCompletionProgress(ulid, count, offset) }
            database.transaction {
                queries.deleteProgress(ulid)
                games.forEach { game ->
                    queries.insertGameIfMissing(game.gameId, game.title, game.consoleId.toLong(), game.consoleName, game.imageIcon)
                    queries.updateGame(
                        title = game.title, consoleId = game.consoleId.toLong(), consoleName = game.consoleName,
                        imageIcon = game.imageIcon, imageBoxArt = null, imageTitle = null, imageIngame = null, publisher = null, developer = null,
                        genre = null, released = null, numPlayers = null, id = game.gameId,
                    )
                    queries.upsertProgress(
                        Game_progress(
                            ulid = ulid,
                            game_id = game.gameId,
                            max_possible = game.maxPossible.toLong(),
                            num_awarded = game.numAwarded.toLong(),
                            num_awarded_hardcore = game.numAwardedHardcore.toLong(),
                            most_recent_awarded = game.mostRecentAwardedDate.toEpochMillis(),
                            highest_award_kind = game.highestAwardKind,
                            highest_award_date = game.highestAwardDate.toEpochMillis(),
                        ),
                    )
                }
            }
        }
    }

    private fun resource() = "library:${accounts.requireUlid()}"
}
