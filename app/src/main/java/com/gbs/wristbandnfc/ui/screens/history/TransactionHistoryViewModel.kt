package com.gbs.wristbandnfc.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbs.wristbandnfc.data.repository.WalletRepository
import com.gbs.wristbandnfc.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionHistoryUiState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionUi> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()

    fun loadTransactions(wristbandId: String) {
        viewModelScope.launch {
            _uiState.value = TransactionHistoryUiState(isLoading = true)

            when (val result = walletRepository.getTransactions(wristbandId)) {
                is Result.Success -> {
                    val transactions = result.data.map { t ->
                        TransactionUi(
                            id = t.id,
                            type = t.type,
                            amount = t.amount,
                            balanceAfter = t.balanceAfter,
                            createdAt = t.createdAt
                        )
                    }
                    _uiState.value = TransactionHistoryUiState(transactions = transactions)
                }
                is Result.Error -> {
                    _uiState.value = TransactionHistoryUiState(error = result.message)
                }
                is Result.Loading -> {}
            }
        }
    }
}
