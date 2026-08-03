package com.example.myapplication.data.repository

import com.example.myapplication.data.local.database.AppDatabase
import com.example.myapplication.data.local.database.entity.UserEntity
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.data.remote.ApiService
import com.example.myapplication.domain.model.AuthResult
import com.example.myapplication.domain.model.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val firebaseAuth: FirebaseAuth,
    private val preferencesRepository: PreferencesRepository,
    private val database: AppDatabase
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResult> {
        return try {
            // Try Firebase Auth first
            val firebaseResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = firebaseResult.user
            val token = user.getIdToken(true).await().token
            
            // Get user profile from backend
            val deviceId = getDeviceId()
            val deviceName = android.os.Build.MODEL
            val apiResponse = apiService.login(
                com.example.myapplication.data.remote.LoginRequest(email, password, deviceId, deviceName)
            )
            
            if (apiResponse.isSuccessful && apiResponse.body() != null) {
                val body = apiResponse.body()!!
                val authResult = AuthResult(
                    user = body.user,
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    expiresIn = body.expiresIn
                )
                
                // Save to local
                saveUserLocally(body.user, body.accessToken, body.refreshToken, body.expiresIn)
                preferencesRepository.setLoggedIn(true).subscribe()
                preferencesRepository.saveUserId(body.user.id).subscribe()
                preferencesRepository.saveUserEmail(body.user.email).subscribe()
                
                Result.Success(authResult)
            } else {
                Result.Error(Exception("Login failed"), apiResponse.message() ?: "Unknown error")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Login failed")
        }
    }

    override suspend fun register(email: String, password: String, displayName: String): Result<AuthResult> {
        return try {
            val firebaseResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = firebaseResult.user
            
            // Update display name
            val profileChangeRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileChangeRequest).await()
            
            val token = user.getIdToken(true).await().token
            
            val deviceId = getDeviceId()
            val deviceName = android.os.Build.MODEL
            val apiResponse = apiService.register(
                com.example.myapplication.data.remote.RegisterRequest(email, password, displayName, deviceId, deviceName)
            )
            
            if (apiResponse.isSuccessful && apiResponse.body() != null) {
                val body = apiResponse.body()!!
                val authResult = AuthResult(
                    user = body.user,
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    expiresIn = body.expiresIn
                )
                
                saveUserLocally(body.user, body.accessToken, body.refreshToken, body.expiresIn)
                preferencesRepository.setLoggedIn(true).subscribe()
                preferencesRepository.saveUserId(body.user.id).subscribe()
                preferencesRepository.saveUserEmail(body.user.email).subscribe()
                preferencesRepository.saveUserDisplayName(displayName).subscribe()
                
                Result.Success(authResult)
            } else {
                Result.Error(Exception("Registration failed"), apiResponse.message() ?: "Unknown error")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Registration failed")
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            apiService.logout().await()
            
            database.userDao().clearAllUsers()
            preferencesRepository.clearAll().subscribe()
            preferencesRepository.setLoggedIn(false).subscribe()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Logout failed")
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val userEntity = database.userDao().getUserByIdSync(firebaseUser.uid)
                if (userEntity != null) {
                    val user = User(
                        id = userEntity.id,
                        email = userEntity.email,
                        displayName = userEntity.displayName,
                        photoUrl = userEntity.photoUrl,
                        isEmailVerified = userEntity.isEmailVerified,
                        createdAt = userEntity.createdAt,
                        lastLoginAt = userEntity.lastLoginAt
                    )
                    Result.Success(user)
                } else {
                    // Fetch from backend
                    val apiResponse = apiService.getProfile().await()
                    if (apiResponse.isSuccessful && apiResponse.body() != null) {
                        val user = apiResponse.body()!!.user
                        saveUserLocally(user, "", null, 0)
                        Result.Success(user)
                    } else {
                        Result.Success(null)
                    }
                }
            } else {
                Result.Success(null)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to get current user")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            apiService.requestPasswordReset(
                com.example.myapplication.data.remote.PasswordResetRequest(email)
            ).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to send reset email")
        }
    }

    override suspend fun updateProfile(displayName: String?, photoUrl: String?): Result<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Result.Error(Exception("No user logged in"), "No user")
            
            val profileChangeRequest = UserProfileChangeRequest.Builder()
            displayName?.let { profileChangeRequest.setDisplayName(it) }
            photoUrl?.let { profileChangeRequest.setPhotoUri(android.net.Uri.parse(it)) }
            firebaseUser.updateProfile(profileChangeRequest.build()).await()
            
            val apiResponse = apiService.updateProfile(
                com.example.myapplication.data.remote.UpdateProfileRequest(displayName, photoUrl)
            ).await()
            
            if (apiResponse.isSuccessful && apiResponse.body() != null) {
                val user = apiResponse.body()!!.user
                updateUserLocally(user)
                displayName?.let { preferencesRepository.saveUserDisplayName(it).subscribe() }
                photoUrl?.let { preferencesRepository.saveUserPhotoUrl(it).subscribe() }
                Result.Success(user)
            } else {
                Result.Error(Exception("Update failed"), apiResponse.message() ?: "Unknown error")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Update failed")
        }
    }

    private suspend fun saveUserLocally(
        user: User,
        accessToken: String,
        refreshToken: String?,
        expiresIn: Long
    ) {
        val entity = UserEntity(
            id = user.id,
            email = user.email,
            displayName = user.displayName,
            photoUrl = user.photoUrl,
            isEmailVerified = user.isEmailVerified,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt,
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000)
        )
        database.userDao().insertUser(entity)
    }

    private suspend fun updateUserLocally(user: User) {
        val existing = database.userDao().getUserByIdSync(user.id)
        if (existing != null) {
            val entity = existing.copy(
                displayName = user.displayName,
                photoUrl = user.photoUrl,
                lastLoginAt = user.lastLoginAt
            )
            database.userDao().updateUser(entity)
        }
    }

    private fun getDeviceId(): String {
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"
    }
}