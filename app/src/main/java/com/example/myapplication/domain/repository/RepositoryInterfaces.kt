package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.model.Result

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResult>
    suspend fun register(email: String, password: String, displayName: String): Result<AuthResult>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun updateProfile(displayName: String?, photoUrl: String?): Result<User>
}

interface UserRepository {
    suspend fun getUserProfile(userId: String): Result<User>
    suspend fun updateUserProfile(user: User): Result<User>
    suspend fun deleteAccount(): Result<Unit>
}

sealed class AuthResult {
    data class Success(
        val user: User,
        val accessToken: String,
        val refreshToken: String?,
        val expiresIn: Long
    ) : AuthResult()
    
    data class Error(val message: String) : AuthResult()
}