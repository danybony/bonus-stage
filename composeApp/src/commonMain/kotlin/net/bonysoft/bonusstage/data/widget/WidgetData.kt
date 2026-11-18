package net.bonysoft.bonusstage.data.widget

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.account.Session
import net.bonysoft.bonusstage.data.repository.LibraryRepository
import net.bonysoft.bonusstage.data.repository.RecentAchievementsRepository
import net.bonysoft.bonusstage.domain.LibraryGame
import net.bonysoft.bonusstage.domain.RecentUnlock

/** What the home screen widget shows. Read from the cache only: widgets never call the API. */
data class WidgetData(
    val username: String?,
    val currentGame: LibraryGame?,
    val recent: List<RecentUnlock>,
) {
    val isSignedIn: Boolean get() = username != null

    companion object {
        val SignedOut = WidgetData(username = null, currentGame = null, recent = emptyList())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class WidgetDataSource(
    private val accounts: AccountRepository,
    private val library: LibraryRepository,
    private val recent: RecentAchievementsRepository,
) {
    fun observe(): Flow<WidgetData> = accounts.session.flatMapLatest { session ->
        if (session !is Session.SignedIn) {
            flowOf(WidgetData.SignedOut)
        } else {
            combine(library.observeLibrary(), recent.observeRecent(limit = RECENT_COUNT)) { games, unlocks ->
                // The library is ordered by latest unlock: its first game is the one being played.
                WidgetData(session.account.username, games.firstOrNull(), unlocks)
            }
        }
    }.distinctUntilChanged()

    private companion object {
        const val RECENT_COUNT = 3
    }
}

/** Redraws the home screen widgets. */
fun interface WidgetUpdater {
    suspend fun update()
}

/** Pushes every cache change that the widget shows to the widgets, from the app or background work. */
@OptIn(FlowPreview::class)
class WidgetSync(source: WidgetDataSource, updater: WidgetUpdater, scope: CoroutineScope) {
    init {
        scope.launch {
            source.observe()
                .drop(1)
                .debounce(500)
                .collect { runCatching { updater.update() } }
        }
    }
}
