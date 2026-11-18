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
import net.bonysoft.bonusstage.db.Active_claim
import net.bonysoft.bonusstage.db.BonusStageDatabase
import net.bonysoft.bonusstage.db.Wishlist
import net.bonysoft.bonusstage.db.Wishlist_entry
import net.bonysoft.bonusstage.domain.SetStatus
import net.bonysoft.bonusstage.domain.WishlistGame
import net.bonysoft.bonusstage.raapi.RaMedia
import kotlin.time.Instant

/** The user's Want to Play list, joined with the active set claims (who is making which set). */
class WishlistRepository(
    private val database: BonusStageDatabase,
    private val accounts: AccountRepository,
    private val refresher: Refresher,
    private val policy: RefreshPolicy,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val queries get() = database.cacheQueries

    fun observeWishlist(): Flow<List<WishlistGame>> =
        queries.wishlist(accounts.requireUlid()).asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toDomain() } }

    fun hasBeenFetched(): Boolean = refresher.fetchedAt("wishlist:${accounts.requireUlid()}") != null

    /** Current wishlist read straight from the cache, for background checks. */
    fun cachedWishlist(): List<WishlistGame> = queries.wishlist(accounts.requireUlid()).executeAsList().map { it.toDomain() }

    suspend fun refresh(force: Boolean = false): Boolean {
        val ulid = accounts.requireUlid()
        return refresher.refresh("wishlist:$ulid", policy.wishlist, force) {
            val client = accounts.client()
            val wishlist = fetchAllPages { count, offset -> client.getUserWantToPlayList(ulid, count, offset) }
            val claims = client.getActiveClaims()
            database.transaction {
                queries.deleteWishlist(ulid)
                wishlist.forEach {
                    queries.insertWishlistEntry(
                        Wishlist_entry(ulid, it.gameId, it.title, it.imageIcon, it.consoleName, it.pointsTotal.toLong(), it.achievementsPublished.toLong()),
                    )
                }
                queries.deleteClaims()
                claims.forEach {
                    queries.insertClaim(
                        Active_claim(it.id, it.gameId, it.user, it.setType.toLong(), it.created.toEpochMillis(), it.doneTime.toEpochMillis()),
                    )
                }
            }
        }
    }

    private fun Wishlist.toDomain() = WishlistGame(
        gameId = game_id,
        title = title,
        iconUrl = RaMedia.image(image_icon),
        consoleName = console_name,
        status = when {
            // A published set stays "published" even while someone works on a revision.
            achievements_published > 0 -> SetStatus.Published(achievements_published.toInt(), points_total.toInt())
            claim_developer != null -> SetStatus.InDevelopment(
                developer = claim_developer,
                isRevision = claim_set_type == 1L,
                expectedBy = claim_done_time?.let(Instant::fromEpochMilliseconds),
            )
            else -> SetStatus.NoSet
        },
    )
}
