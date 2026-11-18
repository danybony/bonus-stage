package net.bonysoft.bonusstage.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.bonysoft.bonusstage.data.repository.CatalogRepository
import net.bonysoft.bonusstage.data.repository.CatalogSync
import net.bonysoft.bonusstage.data.repository.WishlistRepository
import net.bonysoft.bonusstage.data.rom.CatalogGame

data class GameSearchUiState(
    val query: String = "",
    val results: List<CatalogGame> = emptyList(),
    val sync: CatalogSync? = null,
    val error: Throwable? = null,
)

@OptIn(FlowPreview::class)
class GameSearchViewModel(
    private val catalog: CatalogRepository,
    private val wishlist: WishlistRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(GameSearchUiState())
    val state: StateFlow<GameSearchUiState> = _state.asStateFlow()
    private val query = MutableStateFlow("")

    init {
        viewModelScope.launch {
            try {
                catalog.syncAll().collect { sync ->
                    _state.update { it.copy(sync = sync) }
                    // Results grow while consoles arrive.
                    search(query.value)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(error = e) }
            }
        }
        viewModelScope.launch { query.debounce(200).collect { search(it) } }
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        query.value = value
    }

    /** The user is going to the game's page, where they may add it to Want to Play. */
    fun onOpenGame() = wishlist.markStale()

    private suspend fun search(text: String) {
        val results = catalog.search(text)
        _state.update { if (it.query == text) it.copy(results = results) else it }
    }
}
