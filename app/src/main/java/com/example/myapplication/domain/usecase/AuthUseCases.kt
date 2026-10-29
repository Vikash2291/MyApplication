package com.example.myapplication.domain.usecase

import com.example.myapplication.core.common.Validators
import com.example.myapplication.domain.model.AuthResult
import com.example.myapplication.domain.model.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthResult> {
        Validators.validateLogin(email, password)?.let { return Result.Error(it) }
        return authRepository.login(email.trim(), password)
    }
}

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String,
        confirmPassword: String = password
    ): Result<AuthResult> {
        Validators.validateRegistration(displayName, email, password, confirmPassword)
            ?.let { return Result.Error(it) }
        return authRepository.register(email.trim(), password, displayName.trim())
    }
}

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.logout()
}

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User?> = authRepository.getCurrentUser()
}

class ObserveCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<User?> = authRepository.currentUser
}

class UpdateProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(displayName: String?, photoUrl: String?): Result<User> {
        if (displayName != null && !Validators.isValidDisplayName(displayName)) {
            return Result.Error("Enter a valid name")
        }
        return authRepository.updateProfile(displayName?.trim(), photoUrl?.trim()?.ifBlank { null })
    }
}

class SendPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (!Validators.isValidEmail(email)) return Result.Error("Enter a valid email address")
        return authRepository.sendPasswordResetEmail(email.trim())
    }
}

class SendEmailVerificationUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.sendEmailVerification()
}

class DeleteAccountUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(password: String? = null): Result<Unit> {
        if (password != null && password.isBlank()) return Result.Error("Enter your password")
        return userRepository.deleteAccount(password)
    }
}
