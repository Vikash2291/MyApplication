package com.example.myapplication.data.repository

import com.example.myapplication.data.local.database.AppDatabase
import com.example.myapplication.data.local.database.entity.UserEntity
import com.example.myapplication.data.remote.ApiService
import com.example.myapplication.domain.model.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val firebaseAuth: FirebaseAuth,
    private val database: AppDatabase
) : UserRepository {

    override suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            // Try local first
            val localUser = database.userDao().getUserByIdSync(userId)
            if (localUser != null) {
                return Result.Success(mapToUser(localUser))
            }

            // Fetch from backend
            val response = apiService.getProfile().await()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.user
                saveUserLocally(user)
                Result.Success(user)
            } else {
                Result.Error(Exception("Failed to fetch profile"), response.message() ?: "Unknown error")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to get user profile")
        }
    }

    override suspend fun updateUserProfile(user: User): Result<User> {
        return try {
            val response = apiService.updateProfile(
                com.example.myapplication.data.remote.UpdateProfileRequest(user.displayName, user.photoUrl)
            ).await()
            
            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!.user
                saveUserLocally(updatedUser)
                Result.Success(updatedUser)
            } else {
                Result.Error(Exception("Update failed"), response.message() ?: "Unknown error")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Update failed")
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            firebaseUser?.delete()?.await()
            
            apiService.deleteAccount().await()
            
            database.userDao().clearAllUsers()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to delete account")
        }
    }

    private fun mapToUser(entity: UserEntity): User {
        return User(
            id = entity.id,
            email = entity.email,
            displayName = entity.displayName,
            photoUrl = entity.photoUrl,
            isEmailVerified = entity.isEmailVerified,
            createdAt = entity.createdAt,
            lastLoginAt = entity.lastLoginAt
        )
    }

    private suspend fun saveUserLocally(user: User) {
        val entity = UserEntity(
            id = user.id,
            email = user.email,
            displayName = user.displayName,
            photoUrl = user.photoUrl,
            isEmailVerified = user.isEmailVerified,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt,
            accessToken = null,
            refreshToken = null,
            tokenExpiresAt = 0
        )
        database.userDao().insertUser(entity)
    }
}