package net.bonysoft.bonusstage.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import net.bonysoft.bonusstage.data.repository.LibraryRepository
import net.bonysoft.bonusstage.domain.LibraryGame
import net.bonysoft.bonusstage.ui.common.RefreshController
import net.bonysoft.bonusstage.ui.common.RefreshStatus

data class LibraryUiState(
    /** Null until the library has been fetched once. */
    val allGames: List<LibraryGame>? = null,
    val console: Int? = null,
    val statusFilter: StatusFilter = StatusFilter.All,
    val status: RefreshStatus = RefreshStatus(isRefreshing = true),
) {
    val games: List<LibraryGame> get() = allGames.orEmpty().filtered(console, statusFilter)
    val consoles: List<ConsoleOption> get() = allGames.orEmpty().consoleOptions()
}

class LibraryViewModel(private val repository: LibraryRepository) : ViewModel() {

    private val refresher = RefreshController(viewModelScope)
    private val console = MutableStateFlow<Int?>(null)
    private val statusFilter = MutableStateFlow(StatusFilter.All)

    val state: StateFlow<LibraryUiState> = combine(
        repository.observeLibrary(),
        console,
        statusFilter,
        refresher.status,
    ) { games, console, statusFilter, status ->
        LibraryUiState(
            allGames = games.takeIf { it.isNotEmpty() || repository.hasBeenFetched() },
            console = console,
            statusFilter = statusFilter,
            status = status,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    init {
        refresh(force = false)
    }

    fun refresh(force: Boolean = true) = refresher.launch { repository.refresh(force) }

    fun selectConsole(id: Int?) {
        console.value = id
    }

    fun selectStatus(filter: StatusFilter) {
        statusFilter.value = filter
    }
}
