package com.example.myapplication.data.repository

import com.example.myapplication.data.auth.AuthDataSource
import com.example.myapplication.data.auth.AuthNotConfiguredException
import com.example.myapplication.data.device.DeviceInfoProvider
import com.example.myapplication.data.local.database.dao.UserDao
import com.example.myapplication.data.local.database.entity.toDomain
import com.example.myapplication.data.local.database.entity.toEntity
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.data.remote.ApiService
import com.example.myapplication.data.remote.SessionSyncRequest
import com.example.myapplication.data.remote.UpdateProfileRequest
import com.example.myapplication.domain.model.AuthResult
import com.example.myapplication.domain.model.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val TOKEN_LIFETIME_SECONDS = 3600L

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val preferencesRepository: PreferencesRepository,
    private val deviceInfo: DeviceInfoProvider
) : AuthRepository {

    /** Firebase session drives sign-in state; Room supplies the cached profile. */
    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentUser: Flow<User?> = authDataSource.authState
        .map { it?.id }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else userDao.observeUser(id).map { it?.toDomain() ?: authDataSource.currentUser() }
        }

    override suspend fun login(email: String, password: String): Result<AuthResult> = safeCall("Login failed") {
        val user = authDataSource.signIn(email, password)
        Result.Success(completeSession(user))
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthResult> = safeCall("Registration failed") {
        val user = authDataSource.signUp(email, password, displayName)
        Result.Success(completeSession(user))
    }

    override suspend fun logout(): Result<Unit> = safeCall("Logout failed") {
        runCatching { apiService.logout() }
        authDataSource.signOut()
        userDao.clearAllUsers()
        preferencesRepository.clearSession()
        Result.Success(Unit)
    }

    override suspend fun getCurrentUser(): Result<User?> = safeCall("Failed to load user") {
        val sessionUser = authDataSource.currentUser() ?: return@safeCall Result.Success(null)
        val cached = userDao.getUserById(sessionUser.id)?.toDomain()
        Result.Success(cached ?: sessionUser.also { userDao.insertUser(it.toEntity()) })
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        safeCall("Failed to send reset email") {
            authDataSource.sendPasswordReset(email)
            Result.Success(Unit)
        }

    override suspend fun sendEmailVerification(): Result<Unit> =
        safeCall("Failed to send verification email") {
            authDataSource.sendEmailVerification()
            Result.Success(Unit)
        }

    override suspend fun updateProfile(displayName: String?, photoUrl: String?): Result<User> =
        safeCall("Profile update failed") {
            val updated = authDataSource.updateProfile(displayName, photoUrl)
            runCatching { apiService.updateProfile(UpdateProfileRequest(displayName, photoUrl)) }
            val existing = userDao.getUserById(updated.id)
            userDao.insertUser(
                updated.toEntity(
                    accessToken = existing?.accessToken,
                    refreshToken = existing?.refreshToken,
                    tokenExpiresAt = existing?.tokenExpiresAt ?: 0L
                )
            )
            Result.Success(updated)
        }

    private suspend fun completeSession(user: User): AuthResult {
        val token = authDataSource.idToken(forceRefresh = true).orEmpty()
        preferencesRepository.saveAuthToken(token)
        userDao.insertUser(
            user.toEntity(
                accessToken = token,
                tokenExpiresAt = System.currentTimeMillis() + TOKEN_LIFETIME_SECONDS * 1000
            )
        )
        syncWithBackend(user)
        return AuthResult(user = user, accessToken = token, expiresIn = TOKEN_LIFETIME_SECONDS)
    }

    /** Best-effort mirror to the optional backend; failures never block sign-in. */
    private suspend fun syncWithBackend(user: User) {
        try {
            val response = apiService.syncSession(
                SessionSyncRequest(user, deviceInfo.deviceId, deviceInfo.deviceName)
            )
            response.body()?.user?.let { remote ->
                userDao.insertUser(
                    remote.copy(isEmailVerified = user.isEmailVerified).toEntity(
                        accessToken = userDao.getUserById(user.id)?.accessToken
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.d(e, "Backend sync skipped")
        }
    }
}

/** Runs [block], mapping any failure (except cancellation) to [Result.Error]. */
internal suspend fun <T> safeCall(
    fallbackMessage: String,
    block: suspend () -> Result<T>
): Result<T> = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: AuthNotConfiguredException) {
    Result.Error(e.message ?: fallbackMessage, e)
} catch (e: Exception) {
    Timber.w(e, fallbackMessage)
    Result.Error(e.localizedMessage ?: fallbackMessage, e)
}
