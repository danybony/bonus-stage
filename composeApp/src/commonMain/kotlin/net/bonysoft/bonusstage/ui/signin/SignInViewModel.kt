package net.bonysoft.bonusstage.ui.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.bonysoft.bonusstage.data.account.AccountRepository

data class SignInUiState(
    val username: String = "",
    val apiKey: String = "",
    val isChecking: Boolean = false,
    val error: SignInError? = null,
) {
    val canSubmit: Boolean get() = username.isNotBlank() && apiKey.isNotBlank() && !isChecking
}

sealed interface SignInError {
    data object MissingFields : SignInError
    data class Api(val cause: Throwable) : SignInError
}

/** Signing in moves the session to SignedIn; the app root reacts to that, not this screen. */
class SignInViewModel(private val accounts: AccountRepository) : ViewModel() {

    private val _state = MutableStateFlow(SignInUiState())
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    fun onUsernameChange(value: String) = _state.update { it.copy(username = value, error = null) }

    fun onApiKeyChange(value: String) = _state.update { it.copy(apiKey = value, error = null) }

    fun signIn() {
        val current = _state.value
        if (current.isChecking) return
        if (current.username.isBlank() || current.apiKey.isBlank()) {
            _state.update { it.copy(error = SignInError.MissingFields) }
            return
        }
        _state.update { it.copy(isChecking = true, error = null) }
        viewModelScope.launch {
            try {
                accounts.signIn(current.username, current.apiKey)
                _state.update { it.copy(isChecking = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isChecking = false, error = SignInError.Api(e)) }
            }
        }
    }

    fun signInDemo() {
        viewModelScope.launch { accounts.signInDemo() }
    }
}
