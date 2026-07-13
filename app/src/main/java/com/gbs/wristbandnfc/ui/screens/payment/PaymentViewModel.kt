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
import java.util.UUID
import javax.inject.Inject

data class PaymentUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val transactionId: String? = null,
    val newBalance: Long = 0
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val demoRepository: DemoRepository,
    private val demoModeManager: DemoModeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun makePayment(uid: String, token: String, amount: Long) {
        if (amount <= 0) {
            _uiState.value = PaymentUiState(error = "Nominal harus lebih dari 0")
            return
        }

        viewModelScope.launch {
            _uiState.value = PaymentUiState(isLoading = true)

            val isDemoMode = demoModeManager.isDemoModeEnabled.first()

            if (isDemoMode) {
                // Use demo payment
                when (val result = demoRepository.payment(uid, token, amount)) {
                    is Result.Success -> {
                        _uiState.value = PaymentUiState(
                            isSuccess = true,
                            transactionId = result.data.transactionId,
                            newBalance = result.data.newBalance
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = PaymentUiState(error = result.message)
                    }
                    is Result.Loading -> {}
                }
            } else {
                // Use real API
                when (val result = walletRepository.payment(uid, token, amount, "REF-${UUID.randomUUID()}")) {
                    is Result.Success -> {
                        _uiState.value = PaymentUiState(
                            isSuccess = true,
                            transactionId = result.data.transactionId,
                            newBalance = result.data.newBalance
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = PaymentUiState(error = result.message)
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }
}
