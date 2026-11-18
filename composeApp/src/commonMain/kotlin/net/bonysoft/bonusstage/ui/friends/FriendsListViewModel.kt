package net.bonysoft.bonusstage.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.bonysoft.bonusstage.data.repository.FriendsRepository
import net.bonysoft.bonusstage.domain.Player
import net.bonysoft.bonusstage.ui.common.RefreshController
import net.bonysoft.bonusstage.ui.common.RefreshStatus

/** Looking a player up by username. */
data class Lookup(
    val username: String = "",
    val isSearching: Boolean = false,
    val result: Player? = null,
    val error: Throwable? = null,
)

data class FriendsListUiState(
    val following: List<Player> = emptyList(),
    val followers: List<Player> = emptyList(),
    val showFollowers: Boolean = false,
    val lookup: Lookup = Lookup(),
    val status: RefreshStatus = RefreshStatus(isRefreshing = true),
)

class FriendsListViewModel(private val friends: FriendsRepository) : ViewModel() {

    private val refresher = RefreshController(viewModelScope)
    private val showFollowers = MutableStateFlow(false)
    private val lookup = MutableStateFlow(Lookup())

    val state: StateFlow<FriendsListUiState> = combine(
        friends.observeFollowing(),
        friends.observeFollowers(),
        showFollowers,
        lookup,
        refresher.status,
    ) { following, followers, showFollowers, lookup, status ->
        FriendsListUiState(following, followers, showFollowers, lookup, status)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FriendsListUiState())

    /** Called on every resume: refetches only what is stale (or was marked stale by opening a profile). */
    fun refresh(force: Boolean = false) = refresher.launch { friends.refreshLists(force) }

    fun showFollowers(show: Boolean) {
        showFollowers.value = show
    }

    fun onUsernameChange(value: String) = lookup.update { Lookup(username = value) }

    fun find() {
        val username = lookup.value.username.trim()
        if (username.isEmpty() || lookup.value.isSearching) return
        lookup.update { it.copy(isSearching = true, result = null, error = null) }
        viewModelScope.launch {
            try {
                val player = friends.lookUp(username)
                lookup.update { it.copy(isSearching = false, result = player) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lookup.update { it.copy(isSearching = false, error = e) }
            }
        }
    }

    /** The user is going to a profile on the website, where they may follow or unfollow. */
    fun onOpenProfile() = friends.markStale()
}
