package net.bonysoft.bonusstage.ui.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import net.bonysoft.bonusstage.data.repository.RecentAchievementsRepository
import net.bonysoft.bonusstage.ui.common.RefreshController
import net.bonysoft.bonusstage.ui.common.RefreshStatus

data class RecentUiState(
    /** Null until fetched once. */
    val days: List<UnlockDay>? = null,
    val status: RefreshStatus = RefreshStatus(isRefreshing = true),
)

class RecentViewModel(private val repository: RecentAchievementsRepository) : ViewModel() {

    private val refresher = RefreshController(viewModelScope)

    val state: StateFlow<RecentUiState> = combine(repository.observeRecent(), refresher.status) { unlocks, status ->
        RecentUiState(
            days = unlocks.takeIf { it.isNotEmpty() || repository.hasBeenFetched() }?.groupByDay(TimeZone.currentSystemDefault()),
            status = status,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecentUiState())

    init {
        refresh(force = false)
    }

    fun refresh(force: Boolean = true) = refresher.launch { repository.refresh(force) }
}
