package com.example.myapplication.data.repository

import com.example.myapplication.data.auth.AuthDataSource
import com.example.myapplication.data.local.database.dao.UserDao
import com.example.myapplication.data.local.database.entity.toDomain
import com.example.myapplication.data.local.database.entity.toEntity
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.data.remote.ApiService
import com.example.myapplication.domain.model.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val preferencesRepository: PreferencesRepository
) : UserRepository {

    override suspend fun getUserProfile(userId: String): Result<User> = safeCall("Failed to load profile") {
        userDao.getUserById(userId)?.let { return@safeCall Result.Success(it.toDomain()) }

        val remote = runCatching { apiService.getProfile().body()?.user }.getOrNull()
        val user = remote ?: authDataSource.currentUser()?.takeIf { it.id == userId }
        if (user != null) {
            userDao.insertUser(user.toEntity())
            Result.Success(user)
        } else {
            Result.Error("Profile not found")
        }
    }

    override suspend fun refreshUserProfile(): Result<User?> = safeCall("Failed to refresh profile") {
        val fresh = authDataSource.reload() ?: return@safeCall Result.Success(null)
        authDataSource.idToken(forceRefresh = true)?.let { preferencesRepository.saveAuthToken(it) }
        val existing = userDao.getUserById(fresh.id)
        userDao.insertUser(
            fresh.toEntity(
                accessToken = existing?.accessToken,
                refreshToken = existing?.refreshToken,
                tokenExpiresAt = existing?.tokenExpiresAt ?: 0L
            )
        )
        Result.Success(fresh)
    }

    override suspend fun deleteAccount(): Result<Unit> = safeCall("Failed to delete account") {
        runCatching { apiService.deleteAccount() }
        authDataSource.deleteUser()
        authDataSource.signOut()
        userDao.clearAllUsers()
        preferencesRepository.clearSession()
        Result.Success(Unit)
    }
}
