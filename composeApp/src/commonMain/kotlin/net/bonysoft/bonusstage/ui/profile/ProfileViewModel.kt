package net.bonysoft.bonusstage.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.repository.ProfileRepository
import net.bonysoft.bonusstage.data.repository.SettingsRepository
import net.bonysoft.bonusstage.domain.Profile
import net.bonysoft.bonusstage.ui.common.RefreshController
import net.bonysoft.bonusstage.ui.common.RefreshStatus
import net.bonysoft.bonusstage.ui.effects.ScreenEffect

data class ProfileUiState(
    val profile: Profile? = null,
    val isDemo: Boolean = false,
    val screenEffect: ScreenEffect = ScreenEffect.Off,
    val status: RefreshStatus = RefreshStatus(isRefreshing = true),
)

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val accounts: AccountRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val refresher = RefreshController(viewModelScope)

    val state: StateFlow<ProfileUiState> = combine(
        repository.observeProfile(),
        settings.screenEffect,
        refresher.status,
    ) { profile, effect, status ->
        ProfileUiState(profile, accounts.currentAccount?.isDemo == true, effect, status)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    init {
        refresh(force = false)
    }

    fun refresh(force: Boolean = true) = refresher.launch { repository.refresh(force) }

    fun setScreenEffect(effect: ScreenEffect) {
        viewModelScope.launch { settings.setScreenEffect(effect) }
    }

    fun signOut() {
        viewModelScope.launch { accounts.signOut() }
    }
}
