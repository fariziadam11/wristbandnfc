package com.gbs.wristbandnfc.ui.screens.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbs.wristbandnfc.data.model.ScanWristbandResponse
import com.gbs.wristbandnfc.data.repository.DemoRepository
import com.gbs.wristbandnfc.data.repository.WristbandRepository
import com.gbs.wristbandnfc.data.util.DemoModeManager
import com.gbs.wristbandnfc.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanUiState(
    val isScanning: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val nfcEnabled: Boolean = true,
    val scanResult: Pair<String, String>? = null,
    val scannedData: ScanWristbandResponse? = null,
    // Info tentang tag yang berhasil discan
    val scannedUid: String? = null,
    val scannedTechList: String? = null
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val wristbandRepository: WristbandRepository,
    private val demoRepository: DemoRepository,
    private val demoModeManager: DemoModeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private var currentUid: String = ""
    private var currentToken: String = ""

    /**
     * Called when NFC tag is scanned from NfcEventBus
     * Includes UID and tech list info
     */
    fun onNfcTagScanned(uid: String, techList: String = "unknown") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isScanning = true,
                isLoading = true,
                error = null,
                scannedUid = uid,
                scannedTechList = techList
            )

            currentUid = uid
            // For real NFC tags, we construct a token based on UID
            // In production, you would read the token from the NDEF record
            currentToken = "TKN-${uid.takeLast(6).uppercase()}"

            val isDemoMode = demoModeManager.isDemoModeEnabled()

            if (isDemoMode) {
                validateWristbandDemo(currentUid, currentToken)
            } else {
                validateWristband(currentUid, currentToken)
            }
        }
    }

    private suspend fun validateWristband(uid: String, token: String) {
        when (val result = wristbandRepository.scanWristband(uid, token)) {
            is Result.Success -> {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    isLoading = false,
                    scanResult = Pair(uid, token),
                    scannedData = result.data
                )
            }
            is Result.Error -> {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    isLoading = false,
                    error = result.message
                )
            }
            is Result.Loading -> {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }
        }
    }

    private fun validateWristbandDemo(uid: String, token: String) {
        when (val result = demoRepository.scanWristband(uid, token)) {
            is Result.Success -> {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    isLoading = false,
                    scanResult = Pair(uid, token),
                    scannedData = result.data
                )
            }
            is Result.Error -> {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    isLoading = false,
                    error = result.message
                )
            }
            is Result.Loading -> {}
        }
    }

    // For demo purposes - simulate NFC scan with demo data
    fun simulateScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val isDemoMode = demoModeManager.isDemoModeEnabled()

            if (isDemoMode) {
                val demoUid = "DEMO-UID-001"
                val demoToken = "TKN-DEMO1"
                _uiState.value = _uiState.value.copy(
                    scannedUid = demoUid,
                    scannedTechList = "demo"
                )
                validateWristbandDemo(demoUid, demoToken)
            } else {
                val demoUid = "DEMO-UID-001"
                val demoToken = "TKN-DEMO1"
                _uiState.value = _uiState.value.copy(
                    scannedUid = demoUid,
                    scannedTechList = "demo"
                )
                validateWristband(demoUid, demoToken)
            }
        }
    }

    // Scan with specific UID for testing
    fun scanWithUid(uid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            _uiState.value = _uiState.value.copy(scannedUid = uid, scannedTechList = "manual")

            val isDemoMode = demoModeManager.isDemoModeEnabled()
            val token = "TKN-${uid.takeLast(6).uppercase()}"

            if (isDemoMode) {
                validateWristbandDemo(uid, token)
            } else {
                validateWristband(uid, token)
            }
        }
    }

    // Alias for compatibility
    fun simulateScanWithUid(uid: String) = scanWithUid(uid)

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, scannedUid = null, scannedTechList = null)
    }

    fun resetScanResult() {
        _uiState.value = _uiState.value.copy(
            scanResult = null,
            scannedData = null,
            error = null,
            scannedUid = null,
            scannedTechList = null
        )
    }
}
