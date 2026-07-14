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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val usingBackend: Boolean = false
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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Auto-detect backend availability
            val useBackend = !demoModeManager.shouldUseDemoMode()
            _uiState.value = _uiState.value.copy(usingBackend = useBackend)

            if (useBackend) {
                // Use real API
                when (val result = authRepository.login(email, password)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(isSuccess = true, isLoading = false)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(error = result.message, isLoading = false)
                    }
                    is Result.Loading -> {}
                }
            } else {
                // Use demo mode (auto-login)
                when (val result = demoRepository.loginDemo()) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(isSuccess = true, isLoading = false)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(error = result.message, isLoading = false)
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
