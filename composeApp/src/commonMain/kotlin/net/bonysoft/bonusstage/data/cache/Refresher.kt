package net.bonysoft.bonusstage.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.bonysoft.bonusstage.db.BonusStageDatabase
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Runs a fetch only when the cached resource is stale (or forced), records when it ran, and never
 * runs two fetches of the same resource at once.
 */
class Refresher(
    private val database: BonusStageDatabase,
    private val clock: Clock = Clock.System,
) {
    private val locksGuard = Mutex()
    private val locks = mutableMapOf<String, Mutex>()

    fun fetchedAt(resource: String): Instant? =
        database.cacheQueries.fetchedAt(resource).executeAsOneOrNull()?.let(Instant::fromEpochMilliseconds)

    /** Returns true when [fetch] ran. Exceptions from [fetch] propagate and leave the resource stale. */
    suspend fun refresh(resource: String, maxAge: Duration, force: Boolean, fetch: suspend () -> Unit): Boolean {
        val lock = locksGuard.withLock { locks.getOrPut(resource) { Mutex() } }
        return lock.withLock {
            if (!force && !Freshness.isStale(fetchedAt(resource), maxAge, clock.now())) return@withLock false
            fetch()
            database.cacheQueries.markFetched(resource, clock.now().toEpochMilliseconds())
            true
        }
    }
}
