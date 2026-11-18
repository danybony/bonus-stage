package net.bonysoft.bonusstage.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.cache.RefreshPolicy
import net.bonysoft.bonusstage.data.cache.Refresher
import net.bonysoft.bonusstage.data.rom.CatalogGame
import net.bonysoft.bonusstage.db.BonusStageDatabase
import net.bonysoft.bonusstage.db.Catalog_game
import net.bonysoft.bonusstage.db.Catalog_hash
import net.bonysoft.bonusstage.db.Console
import net.bonysoft.bonusstage.raapi.RaMedia

/** Progress of a full catalog download: [done] of [total] consoles. */
data class CatalogSync(val done: Int, val total: Int) {
    val isComplete: Boolean get() = total > 0 && done >= total
}

/**
 * The RetroAchievements game catalog (ADR 0002: refreshed weekly): the console list and, per
 * console, every game with its ROM hashes. One API call per console; the ROM scan and the game
 * search share it.
 */
class CatalogRepository(
    private val database: BonusStageDatabase,
    private val accounts: AccountRepository,
    private val refresher: Refresher,
    private val policy: RefreshPolicy,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val queries get() = database.cacheQueries

    /** Downloads what is stale of the whole catalog, emitting progress per console. */
    fun syncAll(): Flow<CatalogSync> = flow {
        refresher.refresh("consoles", policy.catalog, force = false) {
            val consoles = accounts.client().getConsoleIds(activeOnly = true, gameSystemsOnly = true)
            database.transaction {
                queries.deleteConsoles()
                consoles.forEach { queries.insertConsole(Console(it.id.toLong(), it.name)) }
            }
        }
        val consoles = withContext(dispatcher) { queries.consoles().executeAsList() }
        emit(CatalogSync(0, consoles.size))
        consoles.forEachIndexed { index, console ->
            refreshConsole(console.id.toInt())
            emit(CatalogSync(index + 1, consoles.size))
        }
    }

    /** Fetches one console's games with their hashes, unless cached within the catalog interval. */
    suspend fun refreshConsole(consoleId: Int) {
        refresher.refresh("catalog:$consoleId", policy.catalog, force = false) {
            // Every game, also those without a set: they are the ones worth a wishlist alert.
            val games = accounts.client().getGameList(consoleId, withAchievementsOnly = false, withHashes = true)
            database.transaction {
                queries.deleteCatalogForConsole(consoleId.toLong())
                games.forEach { game ->
                    queries.insertCatalogGame(
                        Catalog_game(
                            game.id, game.consoleId.toLong(), game.consoleName, game.title, game.imageIcon,
                            game.numAchievements.toLong(), game.points.toLong(),
                        ),
                    )
                    game.hashes.forEach { hash -> queries.insertCatalogHash(Catalog_hash(hash.lowercase(), consoleId.toLong(), game.id)) }
                }
            }
        }
    }

    suspend fun search(query: String, limit: Int = 100): List<CatalogGame> {
        val text = query.trim()
        if (text.length < MIN_QUERY) return emptyList()
        return withContext(dispatcher) {
            queries.searchCatalog(text, limit.toLong()).executeAsList().map {
                CatalogGame(
                    it.id, it.title, it.console_id.toInt(), it.console_name, RaMedia.image(it.image_icon),
                    it.num_achievements.toInt(), it.points.toInt(),
                )
            }
        }
    }

    companion object {
        const val MIN_QUERY = 2
    }
}
