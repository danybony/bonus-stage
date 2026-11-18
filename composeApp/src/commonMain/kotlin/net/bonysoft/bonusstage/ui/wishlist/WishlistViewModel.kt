package net.bonysoft.bonusstage.ui.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import net.bonysoft.bonusstage.data.repository.WishlistRepository
import net.bonysoft.bonusstage.domain.SetStatus
import net.bonysoft.bonusstage.domain.WishlistGame
import net.bonysoft.bonusstage.ui.common.RefreshController
import net.bonysoft.bonusstage.ui.common.RefreshStatus

data class WishlistSections(
    val inDevelopment: List<WishlistGame>,
    val ready: List<WishlistGame>,
    val waiting: List<WishlistGame>,
) {
    val isEmpty: Boolean get() = inDevelopment.isEmpty() && ready.isEmpty() && waiting.isEmpty()
}

fun List<WishlistGame>.toSections() = WishlistSections(
    inDevelopment = filter { it.status is SetStatus.InDevelopment },
    ready = filter { it.status is SetStatus.Published },
    waiting = filter { it.status == SetStatus.NoSet },
)

data class WishlistUiState(
    val sections: WishlistSections? = null,
    val status: RefreshStatus = RefreshStatus(isRefreshing = true),
)

class WishlistViewModel(private val repository: WishlistRepository) : ViewModel() {

    private val refresher = RefreshController(viewModelScope)

    val state: StateFlow<WishlistUiState> = combine(repository.observeWishlist(), refresher.status) { games, status ->
        WishlistUiState(games.takeIf { it.isNotEmpty() || repository.hasBeenFetched() }?.toSections(), status)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WishlistUiState())

    init {
        refresh(force = false)
    }

    fun refresh(force: Boolean = true) = refresher.launch { repository.refresh(force) }
}
