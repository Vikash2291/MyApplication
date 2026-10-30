package com.example.myapplication.data.auth

import android.content.Context
import android.net.Uri
import com.example.myapplication.domain.model.ReauthenticationRequiredException
import com.example.myapplication.domain.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : AuthDataSource {

    private val auth: FirebaseAuth? by lazy {
        if (FirebaseApp.getApps(context).isEmpty()) null else FirebaseAuth.getInstance()
    }

    private fun requireAuth(): FirebaseAuth = auth ?: throw AuthNotConfiguredException()

    override val isConfigured: Boolean get() = auth != null

    override val authState: Flow<User?>
        get() {
            val firebaseAuth = auth ?: return flowOf(null)
            return callbackFlow {
                val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toDomain()) }
                firebaseAuth.addAuthStateListener(listener)
                awaitClose { firebaseAuth.removeAuthStateListener(listener) }
            }
        }

    override fun currentUser(): User? = auth?.currentUser?.toDomain()

    override suspend fun signIn(email: String, password: String): User {
        val result = requireAuth().signInWithEmailAndPassword(email, password).await()
        return result.user?.toDomain() ?: error("Sign in failed")
    }

    override suspend fun signUp(email: String, password: String, displayName: String): User {
        val result = requireAuth().createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("Registration failed")
        user.updateProfile(userProfileChangeRequest { this.displayName = displayName }).await()
        runCatching { user.sendEmailVerification().await() }
        user.reload().await()
        return (requireAuth().currentUser ?: user).toDomain()
    }

    override suspend fun idToken(forceRefresh: Boolean): String? =
        auth?.currentUser?.getIdToken(forceRefresh)?.await()?.token

    override suspend fun sendPasswordReset(email: String) {
        requireAuth().sendPasswordResetEmail(email).await()
    }

    override suspend fun sendEmailVerification() {
        val user = requireAuth().currentUser ?: error("No user signed in")
        user.sendEmailVerification().await()
    }

    override suspend fun updateProfile(displayName: String?, photoUrl: String?): User {
        val user = requireAuth().currentUser ?: error("No user signed in")
        user.updateProfile(
            userProfileChangeRequest {
                displayName?.let { this.displayName = it }
                photoUrl?.let { this.photoUri = Uri.parse(it) }
            }
        ).await()
        user.reload().await()
        return (requireAuth().currentUser ?: user).toDomain()
    }

    override suspend fun reload(): User? {
        val user = auth?.currentUser ?: return null
        user.reload().await()
        return auth?.currentUser?.toDomain()
    }

    override suspend fun deleteUser() {
        val user = requireAuth().currentUser ?: error("No user signed in")
        try {
            user.delete().await()
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            throw ReauthenticationRequiredException()
        }
    }

    override suspend fun reauthenticate(password: String) {
        val user = requireAuth().currentUser ?: error("No user signed in")
        val email = user.email ?: error("This account has no email address")
        user.reauthenticate(EmailAuthProvider.getCredential(email, password)).await()
    }

    override fun signOut() {
        auth?.signOut()
    }
}

internal fun FirebaseUser.toDomain(): User = User(
    id = uid,
    email = email.orEmpty(),
    displayName = displayName,
    photoUrl = photoUrl?.toString(),
    isEmailVerified = isEmailVerified,
    createdAt = metadata?.creationTimestamp ?: 0L,
    lastLoginAt = metadata?.lastSignInTimestamp ?: System.currentTimeMillis()
)
