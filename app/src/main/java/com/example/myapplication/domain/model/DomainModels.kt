package com.example.myapplication.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: Long = 0L,
    val lastLoginAt: Long = 0L
)

data class AuthResult(
    val user: User,
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresIn: Long = 0L
)

/**
 * Thrown when a sensitive operation (e.g. account deletion) needs the user to confirm their
 * password again because the last sign-in is too old.
 */
class ReauthenticationRequiredException(
    message: String = "For your security, please enter your password again to continue."
) : Exception(message)

/** UI theme selection persisted in preferences. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Wrapper for the outcome of a repository / use-case call. */
sealed interface Result<out T> {
    data class Success<out T>(val data: T) : Result<T>
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
}
