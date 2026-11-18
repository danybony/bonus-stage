package net.bonysoft.bonusstage.ui.common

import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RefreshStatus(val isRefreshing: Boolean = false, val error: Throwable? = null)

/** Runs one refresh at a time for a screen and exposes whether it is running and how it failed. */
class RefreshController(private val scope: CoroutineScope) {
    private val _status = MutableStateFlow(RefreshStatus())
    val status: StateFlow<RefreshStatus> = _status.asStateFlow()

    fun launch(block: suspend () -> Unit) {
        if (_status.value.isRefreshing) return
        _status.value = RefreshStatus(isRefreshing = true)
        scope.launch {
            try {
                block()
                _status.value = RefreshStatus()
            } catch (e: CancellationException) {
                _status.update { it.copy(isRefreshing = false) }
                throw e
            } catch (e: Exception) {
                _status.value = RefreshStatus(error = e)
            }
        }
    }
}
