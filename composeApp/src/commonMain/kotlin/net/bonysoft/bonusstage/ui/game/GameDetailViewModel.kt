package net.bonysoft.bonusstage.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.bonysoft.bonusstage.data.repository.GameRepository
import net.bonysoft.bonusstage.data.repository.SettingsRepository
import net.bonysoft.bonusstage.domain.GameDetail
import net.bonysoft.bonusstage.ui.common.RefreshController
import net.bonysoft.bonusstage.ui.common.RefreshStatus

data class GameDetailUiState(
    val game: GameDetail? = null,
    val hideSpoilers: Boolean = true,
    val status: RefreshStatus = RefreshStatus(isRefreshing = true),
)

class GameDetailViewModel(
    private val gameId: Long,
    private val games: GameRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val refresher = RefreshController(viewModelScope)

    val state: StateFlow<GameDetailUiState> = combine(
        games.observeGame(gameId),
        settings.hideSpoilers,
        refresher.status,
    ) { game, hideSpoilers, status ->
        GameDetailUiState(game, hideSpoilers, status)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameDetailUiState())

    init {
        refresh(force = false)
    }

    fun refresh(force: Boolean = true) = refresher.launch { games.refresh(gameId, force) }

    fun setHideSpoilers(hide: Boolean) {
        viewModelScope.launch { settings.setHideSpoilers(hide) }
    }
}
