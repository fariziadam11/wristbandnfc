package com.gbs.wristbandnfc.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbs.wristbandnfc.data.repository.WristbandRepository
import com.gbs.wristbandnfc.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterWriteUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val token: String? = null
)

@HiltViewModel
class RegisterWriteViewModel @Inject constructor(
    private val wristbandRepository: WristbandRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterWriteUiState())
    val uiState: StateFlow<RegisterWriteUiState> = _uiState.asStateFlow()

    fun writeNfc(uid: String) {
        viewModelScope.launch {
            _uiState.value = RegisterWriteUiState(isLoading = true)

            when (val result = wristbandRepository.writeWristband(uid)) {
                is Result.Success -> {
                    _uiState.value = RegisterWriteUiState(
                        isSuccess = true,
                        token = result.data.token
                    )
                }
                is Result.Error -> {
                    _uiState.value = RegisterWriteUiState(error = result.message)
                }
                is Result.Loading -> {}
            }
        }
    }
}
