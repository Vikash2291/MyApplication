package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.AuthResult
import com.example.myapplication.domain.model.Result
import com.example.myapplication.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Emits the signed-in user, or null when signed out. */
    val currentUser: Flow<User?>

    suspend fun login(email: String, password: String): Result<AuthResult>
    suspend fun register(email: String, password: String, displayName: String): Result<AuthResult>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun updateProfile(displayName: String?, photoUrl: String?): Result<User>
}

interface UserRepository {
    suspend fun getUserProfile(userId: String): Result<User>
    suspend fun refreshUserProfile(): Result<User?>
    /**
     * Permanently deletes the account and all local data, then signs out. Pass [password] to
     * re-authenticate first (after a [com.example.myapplication.domain.model.ReauthenticationRequiredException]).
     */
    suspend fun deleteAccount(password: String? = null): Result<Unit>
}
