package net.bonysoft.bonusstage.data.rom

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.cache.RefreshPolicy
import net.bonysoft.bonusstage.data.cache.Refresher
import net.bonysoft.bonusstage.db.BonusStageDatabase
import net.bonysoft.bonusstage.db.Catalog_game
import net.bonysoft.bonusstage.db.Catalog_hash
import net.bonysoft.bonusstage.raapi.RaMedia

data class CatalogGame(
    val id: Long,
    val title: String,
    val consoleId: Int,
    val consoleName: String,
    val iconUrl: String,
    val achievements: Int,
    val points: Int,
)

data class MatchedRom(val file: RomFile, val game: CatalogGame)

data class ScanResult(val matched: List<MatchedRom>, val unknown: List<RomFile>, val scanned: Int)

sealed interface ScanProgress {
    data object Listing : ScanProgress
    data class Hashing(val done: Int, val total: Int) : ScanProgress
    data class LoadingCatalog(val consoles: Int) : ScanProgress
    data class Done(val result: ScanResult) : ScanProgress
}

/**
 * Pure matching: a file matches the first game whose catalog lists one of its hashes for the
 * same console. Each game is reported once, with the first file that matched it.
 */
fun matchRoms(hashed: List<Pair<RomFile, List<RomHash>>>, catalog: Map<RomHash, CatalogGame>): ScanResult {
    val matched = mutableListOf<MatchedRom>()
    val unknown = mutableListOf<RomFile>()
    val seenGames = mutableSetOf<Long>()
    hashed.forEach { (file, hashes) ->
        val game = hashes.firstNotNullOfOrNull { catalog[it] }
        when {
            game == null -> unknown += file
            seenGames.add(game.id) -> matched += MatchedRom(file, game)
        }
    }
    return ScanResult(matched.sortedBy { it.game.title.lowercase() }, unknown.sortedBy { it.name.lowercase() }, hashed.size)
}

/**
 * Scans a ROM folder (issue #17): hashes every candidate file with rhash, then matches the hashes
 * against the catalog of the consoles found. The catalog with hashes costs one API call per
 * console and is cached for [RefreshPolicy.catalog].
 */
class RomScanRepository(
    private val library: RomLibrary,
    private val database: BonusStageDatabase,
    private val accounts: AccountRepository,
    private val refresher: Refresher,
    private val policy: RefreshPolicy,
) {
    private val queries get() = database.cacheQueries

    fun scan(folder: String): Flow<ScanProgress> = flow {
        emit(ScanProgress.Listing)
        val files = library.listFiles(folder).filter { it.isCandidate() }
        val hashed = files.mapIndexed { index, file ->
            emit(ScanProgress.Hashing(index, files.size))
            file to library.hash(file)
        }
        val consoles = hashed.flatMap { (_, hashes) -> hashes.map { it.consoleId } }.distinct()
        emit(ScanProgress.LoadingCatalog(consoles.size))
        consoles.forEach { refreshCatalog(it) }
        val catalog = consoles.flatMap { console ->
            val hashes = hashed.flatMap { it.second }.filter { it.consoleId == console }.map { it.hash }.distinct()
            hashes.chunked(500).flatMap { chunk -> queries.catalogMatches(console.toLong(), chunk).executeAsList() }
                .map { row ->
                    RomHash(console, row.hash) to CatalogGame(
                        row.id, row.title, row.console_id.toInt(), row.console_name, RaMedia.image(row.image_icon),
                        row.num_achievements.toInt(), row.points.toInt(),
                    )
                }
        }.toMap()
        emit(ScanProgress.Done(matchRoms(hashed, catalog)))
    }

    private suspend fun refreshCatalog(consoleId: Int) {
        refresher.refresh("catalog:$consoleId", policy.catalog, force = false) {
            val games = accounts.client().getGameList(consoleId, withAchievementsOnly = true, withHashes = true)
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
}
