package com.gbs.wristbandnfc.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbs.wristbandnfc.data.model.RegisterWristbandResponse
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

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val registerResult: RegisterWristbandResponse? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val wristbandRepository: WristbandRepository,
    private val demoRepository: DemoRepository,
    private val demoModeManager: DemoModeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun registerWristband(customerName: String, customerEmail: String?) {
        if (customerName.isBlank()) {
            _uiState.value = RegisterUiState(error = "Nama customer harus diisi")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)

            val isDemoMode = demoModeManager.isDemoModeEnabled.first()

            if (isDemoMode) {
                // Use demo registration
                when (val result = demoRepository.registerWristband(customerName)) {
                    is Result.Success -> {
                        _uiState.value = RegisterUiState(registerResult = result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = RegisterUiState(error = result.message)
                    }
                    is Result.Loading -> {}
                }
            } else {
                // Use real API
                when (val result = wristbandRepository.registerWristband(customerName, customerEmail)) {
                    is Result.Success -> {
                        _uiState.value = RegisterUiState(registerResult = result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = RegisterUiState(error = result.message)
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }
}
