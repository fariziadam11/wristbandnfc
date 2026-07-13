package com.gbs.wristbandnfc.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbs.wristbandnfc.data.repository.AuthRepository
import com.gbs.wristbandnfc.data.repository.DemoRepository
import com.gbs.wristbandnfc.data.util.DemoModeManager
import com.gbs.wristbandnfc.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val demoRepository: DemoRepository,
    private val demoModeManager: DemoModeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(error = "Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            // Check if demo mode is enabled
            val isDemoMode = demoModeManager.isDemoModeEnabled.first()

            if (isDemoMode) {
                // Use demo login
                when (val result = demoRepository.loginDemo()) {
                    is Result.Success -> {
                        _uiState.value = LoginUiState(isSuccess = true)
                    }
                    is Result.Error -> {
                        _uiState.value = LoginUiState(error = result.message)
                    }
                    is Result.Loading -> {}
                }
            } else {
                // Use real API login
                when (val result = authRepository.login(email, password)) {
                    is Result.Success -> {
                        _uiState.value = LoginUiState(isSuccess = true)
                    }
                    is Result.Error -> {
                        _uiState.value = LoginUiState(error = result.message)
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
