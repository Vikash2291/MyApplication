package com.example.myapplication.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean,
    val createdAt: Long,
    val lastLoginAt: Long
) {
    companion object {
        fun empty(): User = User(
            id = "",
            email = "",
            displayName = null,
            photoUrl = null,
            isEmailVerified = false,
            createdAt = 0,
            lastLoginAt = 0
        )
    }
}

@Serializable
data class AuthResult(
    val user: User,
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long
)

@Serializable
sealed class Result<out T> {
    @Serializable
    data class Success<out T>(val data: T) : Result<T>()
    
    @Serializable
    data class Error(val exception: Throwable, val message: String) : Result<Nothing>()
    
    @Serializable
    object Loading : Result<Nothing>()
}

@Serializable
data class ApiError(
    val code: Int,
    val message: String,
    val details: String?
)