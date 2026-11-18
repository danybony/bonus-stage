package net.bonysoft.bonusstage.data.repository

import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.account.UserDataCleaner
import net.bonysoft.bonusstage.data.cache.RaDates
import net.bonysoft.bonusstage.db.BonusStageDatabase
import net.bonysoft.bonusstage.raapi.RaApiException

internal fun AccountRepository.requireUlid(): String = currentAccount?.ulid ?: throw RaApiException.Unauthorized()

internal fun String?.toEpochMillis(): Long? = RaDates.parse(this)?.toEpochMilliseconds()

internal fun Boolean.toLong(): Long = if (this) 1 else 0

/** Fetches every page of a paginated endpoint. */
internal suspend fun <T> fetchAllPages(pageSize: Int = 500, fetch: suspend (count: Int, offset: Int) -> net.bonysoft.bonusstage.raapi.model.Page<T>): List<T> {
    val all = mutableListOf<T>()
    while (true) {
        val page = fetch(pageSize, all.size)
        all += page.results
        if (page.results.isEmpty() || all.size >= page.total) return all
    }
}

/** Forgets every cached row on sign-out. */
class CacheCleaner(private val database: BonusStageDatabase) : UserDataCleaner {
    override suspend fun clear() {
        database.cacheQueries.clearAll()
    }
}
