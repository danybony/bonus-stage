package net.bonysoft.bonusstage.ui.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import net.bonysoft.bonusstage.data.repository.FriendsRepository
import net.bonysoft.bonusstage.domain.FriendUnlock
import net.bonysoft.bonusstage.ui.common.RefreshController
import net.bonysoft.bonusstage.ui.common.RefreshStatus

data class FriendsUiState(
    /** Null until the follow list has been fetched once. */
    val days: List<Day<FriendUnlock>>? = null,
    val following: Int = 0,
    val status: RefreshStatus = RefreshStatus(isRefreshing = true),
)

class FriendsViewModel(private val repository: FriendsRepository) : ViewModel() {

    private val refresher = RefreshController(viewModelScope)

    val state: StateFlow<FriendsUiState> = combine(
        repository.observeFeed(),
        repository.observeFollowingCount(),
        refresher.status,
    ) { feed, following, status ->
        FriendsUiState(
            days = feed.takeIf { it.isNotEmpty() || repository.hasBeenFetched() }
                ?.groupByDay(TimeZone.currentSystemDefault()) { it.unlockedAt },
            following = following,
            status = status,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FriendsUiState())

    init {
        refresh(force = false)
    }

    fun refresh(force: Boolean = true) = refresher.launch { repository.refresh(force) }
}
