package net.bonysoft.bonusstage.ui.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.repository.MonthlyRecap
import net.bonysoft.bonusstage.data.repository.RecapRepository
import kotlin.time.Clock

data class RecapUiState(
    val lastMonth: Boolean = false,
    val recap: MonthlyRecap? = null,
    val username: String = "",
    val error: Throwable? = null,
    val isLoading: Boolean = true,
)

class RecapViewModel(private val recaps: RecapRepository, private val accounts: AccountRepository) : ViewModel() {

    private val _state = MutableStateFlow(RecapUiState(username = accounts.currentAccount?.username.orEmpty()))
    val state: StateFlow<RecapUiState> = _state.asStateFlow()

    init {
        select(lastMonth = false)
    }

    fun select(lastMonth: Boolean) {
        _state.update { it.copy(lastMonth = lastMonth, recap = null, error = null, isLoading = true) }
        viewModelScope.launch {
            try {
                val recap = recaps.recap(month(lastMonth))
                _state.update { if (it.lastMonth == lastMonth) it.copy(recap = recap, isLoading = false) else it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { if (it.lastMonth == lastMonth) it.copy(error = e, isLoading = false) else it }
            }
        }
    }

    private fun month(lastMonth: Boolean): YearMonth {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return (if (lastMonth) today.minus(DatePeriod(months = 1)) else today).yearMonth
    }
}
