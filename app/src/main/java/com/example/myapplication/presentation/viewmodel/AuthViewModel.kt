package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.AuthResult
import com.example.myapplication.domain.model.ReauthenticationRequiredException
import com.example.myapplication.domain.model.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.usecase.DeleteAccountUseCase
import com.example.myapplication.domain.usecase.LoginUseCase
import com.example.myapplication.domain.usecase.LogoutUseCase
import com.example.myapplication.domain.usecase.ObserveCurrentUserUseCase
import com.example.myapplication.domain.usecase.RegisterUseCase
import com.example.myapplication.domain.usecase.SendEmailVerificationUseCase
import com.example.myapplication.domain.usecase.SendPasswordResetUseCase
import com.example.myapplication.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    /** Account deletion needs the password again (Firebase "requires recent login"). */
    val isReauthRequired: Boolean = false
) {
    val isLoggedIn: Boolean get() = user != null
}

sealed interface AuthEvent {
    data object NavigateToHome : AuthEvent
    data object NavigateToLogin : AuthEvent
    data class ShowMessage(val message: String) : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase,
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    observeCurrentUser: ObserveCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeCurrentUser().collect { user -> _uiState.update { it.copy(user = user) } }
        }
    }

    fun login(email: String, password: String) = runAuth { loginUseCase(email, password) }

    fun register(email: String, password: String, confirmPassword: String, displayName: String) =
        runAuth { registerUseCase(email, password, displayName, confirmPassword) }

    fun logout() = launchWithLoading {
        when (val result = logoutUseCase()) {
            is Result.Success -> {
                _uiState.update { AuthUiState() }
                _events.send(AuthEvent.NavigateToLogin)
            }
            is Result.Error -> setError(result.message)
        }
    }

    /** Deletes the account; pass [password] when [AuthUiState.isReauthRequired] asked for it. */
    fun deleteAccount(password: String? = null) = launchWithLoading {
        when (val result = deleteAccountUseCase(password)) {
            is Result.Success -> {
                _uiState.update { AuthUiState() }
                _events.send(AuthEvent.ShowMessage("Account deleted"))
                _events.send(AuthEvent.NavigateToLogin)
            }
            is Result.Error -> if (result.exception is ReauthenticationRequiredException) {
                _uiState.update { it.copy(isReauthRequired = true, error = null) }
            } else {
                setError(result.message)
            }
        }
    }

    fun cancelReauth() = _uiState.update { it.copy(isReauthRequired = false) }

    fun sendPasswordReset(email: String) = launchWithLoading {
        when (val result = sendPasswordResetUseCase(email)) {
            is Result.Success -> _events.send(AuthEvent.ShowMessage("Password reset email sent"))
            is Result.Error -> setError(result.message)
        }
    }

    fun sendEmailVerification() = launchWithLoading {
        when (val result = sendEmailVerificationUseCase()) {
            is Result.Success -> _events.send(AuthEvent.ShowMessage("Verification email sent"))
            is Result.Error -> setError(result.message)
        }
    }

    fun updateProfile(displayName: String?, photoUrl: String?) = launchWithLoading {
        when (val result = updateProfileUseCase(displayName, photoUrl)) {
            is Result.Success -> {
                _uiState.update { it.copy(user = result.data) }
                _events.send(AuthEvent.ShowMessage("Profile updated"))
            }
            is Result.Error -> setError(result.message)
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun runAuth(block: suspend () -> Result<AuthResult>) = launchWithLoading {
        when (val result = block()) {
            is Result.Success -> {
                _uiState.update { it.copy(user = result.data.user) }
                _events.send(AuthEvent.NavigateToHome)
            }
            is Result.Error -> setError(result.message)
        }
    }

    private fun launchWithLoading(block: suspend () -> Unit) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                block()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun setError(message: String) = _uiState.update { it.copy(error = message) }
}
