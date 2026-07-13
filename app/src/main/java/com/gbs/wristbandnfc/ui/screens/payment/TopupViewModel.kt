package com.gbs.wristbandnfc.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbs.wristbandnfc.data.repository.DemoRepository
import com.gbs.wristbandnfc.data.repository.WalletRepository
import com.gbs.wristbandnfc.data.util.DemoModeManager
import com.gbs.wristbandnfc.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopupUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val transactionId: String? = null,
    val newBalance: Long = 0
)

@HiltViewModel
class TopupViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val demoRepository: DemoRepository,
    private val demoModeManager: DemoModeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopupUiState())
    val uiState: StateFlow<TopupUiState> = _uiState.asStateFlow()

    fun makeTopup(uid: String, token: String, amount: Long) {
        if (amount <= 0) {
            _uiState.value = TopupUiState(error = "Nominal harus lebih dari 0")
            return
        }

        viewModelScope.launch {
            _uiState.value = TopupUiState(isLoading = true)

            val isDemoMode = demoModeManager.isDemoModeEnabled.first()

            if (isDemoMode) {
                // Use demo topup
                when (val result = demoRepository.topup(uid, token, amount)) {
                    is Result.Success -> {
                        _uiState.value = TopupUiState(
                            isSuccess = true,
                            transactionId = result.data.transactionId,
                            newBalance = result.data.newBalance
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = TopupUiState(error = result.message)
                    }
                    is Result.Loading -> {}
                }
            } else {
                // Use real API
                when (val result = walletRepository.topup(uid, token, amount)) {
                    is Result.Success -> {
                        _uiState.value = TopupUiState(
                            isSuccess = true,
                            transactionId = result.data.transactionId,
                            newBalance = result.data.newBalance
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = TopupUiState(error = result.message)
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }
}
