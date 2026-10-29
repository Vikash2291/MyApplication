package com.example.myapplication.data.auth

import com.example.myapplication.domain.model.User
import kotlinx.coroutines.flow.Flow

/** Abstraction over the identity provider so repositories stay unit-testable. */
interface AuthDataSource {
    val isConfigured: Boolean
    val authState: Flow<User?>
    fun currentUser(): User?
    suspend fun signIn(email: String, password: String): User
    suspend fun signUp(email: String, password: String, displayName: String): User
    suspend fun idToken(forceRefresh: Boolean = false): String?
    suspend fun sendPasswordReset(email: String)
    suspend fun sendEmailVerification()
    suspend fun updateProfile(displayName: String?, photoUrl: String?): User
    suspend fun reload(): User?
    suspend fun deleteUser()
    fun signOut()
}

class AuthNotConfiguredException : IllegalStateException(
    "Firebase is not configured. Add app/google-services.json and rebuild."
)
