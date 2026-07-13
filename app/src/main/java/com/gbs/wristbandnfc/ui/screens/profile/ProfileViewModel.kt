package com.gbs.wristbandnfc.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbs.wristbandnfc.data.repository.AuthRepository
import com.gbs.wristbandnfc.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val userName: String? = null,
    val userEmail: String? = null,
    val createdAt: String? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true)

            when (val result = authRepository.getProfile()) {
                is Result.Success -> {
                    val user = result.data
                    _uiState.value = ProfileUiState(
                        userName = user.name,
                        userEmail = user.email,
                        createdAt = user.createdAt
                    )
                }
                is Result.Error -> {
                    _uiState.value = ProfileUiState(error = result.message)
                }
                is Result.Loading -> {}
            }
        }
    }
}
