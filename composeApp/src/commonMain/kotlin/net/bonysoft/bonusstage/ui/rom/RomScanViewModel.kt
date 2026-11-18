package net.bonysoft.bonusstage.ui.rom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.bonysoft.bonusstage.data.repository.SettingsRepository
import net.bonysoft.bonusstage.data.rom.RomScanRepository
import net.bonysoft.bonusstage.data.rom.ScanProgress

data class RomScanUiState(
    val folder: String? = null,
    val progress: ScanProgress? = null,
    val error: Throwable? = null,
) {
    val isScanning: Boolean get() = progress != null && progress !is ScanProgress.Done
}

class RomScanViewModel(
    private val scanner: RomScanRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RomScanUiState())
    val state: StateFlow<RomScanUiState> = _state.asStateFlow()
    private var job: Job? = null

    init {
        viewModelScope.launch { _state.update { it.copy(folder = settings.romFolder.first()) } }
    }

    fun pick(folder: String) {
        viewModelScope.launch { settings.setRomFolder(folder) }
        _state.update { it.copy(folder = folder) }
        scan()
    }

    fun scan() {
        val folder = _state.value.folder ?: return
        job?.cancel()
        _state.update { it.copy(progress = ScanProgress.Listing, error = null) }
        job = viewModelScope.launch {
            try {
                scanner.scan(folder).collect { progress -> _state.update { it.copy(progress = progress) } }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(progress = null, error = e) }
            }
        }
    }
}
