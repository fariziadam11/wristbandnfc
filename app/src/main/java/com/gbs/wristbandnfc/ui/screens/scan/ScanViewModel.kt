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
    val nfcEnabled: Boolean = false,
    val scanResult: Pair<String, String>? = null, // uid, token
    val scannedData: ScanWristbandResponse? = null
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

    fun handleNfcIntent(uid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, isLoading = true)

            val isDemoMode = demoModeManager.isDemoModeEnabled.first()

            if (isDemoMode) {
                // Use demo scan
                currentUid = uid
                currentToken = "TKN-${uid.takeLast(6)}"
                validateWristbandDemo(currentUid, currentToken)
            } else {
                // Use real API
                currentUid = uid
                currentToken = "DEMO-TOKEN"
                validateWristband(currentUid, currentToken)
            }
        }
    }

    private fun validateWristband(uid: String, token: String) {
        viewModelScope.launch {
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

            val isDemoMode = demoModeManager.isDemoModeEnabled.first()

            if (isDemoMode) {
                // Use demo data
                val demoUid = "DEMO-UID-001"
                val demoToken = "TKN-DEMO1"
                validateWristbandDemo(demoUid, demoToken)
            } else {
                // Use real API
                val demoUid = "DEMO-UID-001"
                val demoToken = "TKN-DEMO1"
                validateWristband(demoUid, demoToken)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
