package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.usecase.GetCurrentUserUseCase
import com.example.myapplication.domain.usecase.LoginUseCase
import com.example.myapplication.domain.usecase.LogoutUseCase
import com.example.myapplication.domain.usecase.RegisterUseCase
import com.example.myapplication.domain.usecase.SendPasswordResetUseCase
import com.example.myapplication.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    // State
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: kotlinx.coroutines.flow.StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Events
    private val _events = MutableStateFlow<AuthEvent?>(null)
    val events: kotlinx.coroutines.flow.StateFlow<AuthEvent?> = _events.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = loginUseCase(email, password)
            handleAuthResult(result)
        }
    }

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = registerUseCase(email, password, displayName)
            handleAuthResult(result)
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = logoutUseCase()
            when (result) {
                is Result.Success -> {
                    _uiState.update { AuthUiState() }
                    _events.value = AuthEvent.NavigateToLogin
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = sendPasswordResetUseCase(email)
            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.value = AuthEvent.ShowMessage("Password reset email sent")
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun updateProfile(displayName: String?, photoUrl: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = updateProfileUseCase(displayName, photoUrl)
            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, user = result.data) }
                    _events.value = AuthEvent.ShowMessage("Profile updated")
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            val result = getCurrentUserUseCase()
            when (result) {
                is Result.Success -> _uiState.update { it.copy(user = result.data, isLoggedIn = result.data != null) }
                is Result.Error -> _uiState.update { it.copy(isLoggedIn = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun handleAuthResult(result: Result<com.example.myapplication.domain.model.AuthResult>) {
        when (result) {
            is Result.Success -> {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        isLoggedIn = true, 
                        user = result.data.user,
                        error = null
                    ) 
                }
                _events.value = AuthEvent.NavigateToHome
            }
            is Result.Error -> {
                _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    // Consume event
    fun consumeEvent(): AuthEvent? {
        return _events.value?.also { _events.value = null }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

sealed interface AuthEvent {
    data class NavigateToHome : AuthEvent
    data class NavigateToLogin : AuthEvent
    data class ShowMessage(val message: String) : AuthEvent
    data class ShowError(val error: String) : AuthEvent
}