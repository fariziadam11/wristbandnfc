package com.gbs.wristbandnfc.ui.screens.wallet

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

data class WalletUiState(
    val isLoading: Boolean = false,
    val balance: Long = 0,
    val currency: String = "IDR",
    val customerName: String? = null,
    val wristbandStatus: String? = null,
    val wristbandId: String? = null,
    val error: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val wristbandRepository: WristbandRepository,
    private val demoRepository: DemoRepository,
    private val demoModeManager: DemoModeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private var currentUid: String = ""
    private var currentToken: String = ""

    fun loadWalletData(uid: String, token: String) {
        currentUid = uid
        currentToken = token

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val isDemoMode = demoModeManager.isDemoModeEnabled()

            if (isDemoMode) {
                // Use demo data
                when (val result = demoRepository.scanWristband(uid, token)) {
                    is Result.Success -> {
                        val data = result.data
                        _uiState.value = WalletUiState(
                            balance = data.wallet.balance,
                            currency = data.wallet.currency,
                            customerName = data.customer.name,
                            wristbandStatus = data.status,
                            wristbandId = data.wristbandId
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = WalletUiState(error = result.message)
                    }
                    is Result.Loading -> {}
                }
            } else {
                // Use real API
                when (val result = wristbandRepository.scanWristband(uid, token)) {
                    is Result.Success -> {
                        val data = result.data
                        _uiState.value = WalletUiState(
                            balance = data.wallet.balance,
                            currency = data.wallet.currency,
                            customerName = data.customer.name,
                            wristbandStatus = data.status,
                            wristbandId = data.wristbandId
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = WalletUiState(error = result.message)
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    fun refreshData() {
        if (currentUid.isNotEmpty() && currentToken.isNotEmpty()) {
            loadWalletData(currentUid, currentToken)
        }
    }
}
