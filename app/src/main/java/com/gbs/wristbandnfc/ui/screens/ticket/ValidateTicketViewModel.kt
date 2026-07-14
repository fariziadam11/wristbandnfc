package com.gbs.wristbandnfc.ui.screens.ticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbs.wristbandnfc.data.model.ValidateTicketResponse
import com.gbs.wristbandnfc.data.repository.DemoRepository
import com.gbs.wristbandnfc.data.repository.TicketRepository
import com.gbs.wristbandnfc.data.util.DemoModeManager
import com.gbs.wristbandnfc.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ValidateTicketUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: ValidateTicketResponse? = null
)

@HiltViewModel
class ValidateTicketViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val demoRepository: DemoRepository,
    private val demoModeManager: DemoModeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ValidateTicketUiState())
    val uiState: StateFlow<ValidateTicketUiState> = _uiState.asStateFlow()

    fun validateTicket(uid: String, token: String) {
        viewModelScope.launch {
            _uiState.value = ValidateTicketUiState(isLoading = true)

            val isDemoMode = demoModeManager.isDemoModeEnabled()

            if (isDemoMode) {
                // Use demo validation
                when (val result = demoRepository.validateTicket(uid, token)) {
                    is Result.Success -> {
                        _uiState.value = ValidateTicketUiState(result = result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = ValidateTicketUiState(error = result.message)
                    }
                    is Result.Loading -> {}
                }
            } else {
                // Use real API
                when (val result = ticketRepository.validateTicket(uid, token)) {
                    is Result.Success -> {
                        _uiState.value = ValidateTicketUiState(result = result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = ValidateTicketUiState(error = result.message)
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }
}
