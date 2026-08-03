package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.model.AuthResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthUseCasesTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var registerUseCase: RegisterUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var sendPasswordResetUseCase: SendPasswordResetUseCase
    private lateinit var updateProfileUseCase: UpdateProfileUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        loginUseCase = LoginUseCase(authRepository)
        registerUseCase = RegisterUseCase(authRepository)
        logoutUseCase = LogoutUseCase(authRepository)
        getCurrentUserUseCase = GetCurrentUserUseCase(authRepository)
        sendPasswordResetUseCase = SendPasswordResetUseCase(authRepository)
        updateProfileUseCase = UpdateProfileUseCase(authRepository)
    }

    @Test
    fun login_success_returnsAuthResult() = runBlocking {
        val expectedUser = User(
            id = "123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null,
            isEmailVerified = true,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )
        val expectedAuthResult = AuthResult(
            user = expectedUser,
            accessToken = "access_token",
            refreshToken = "refresh_token",
            expiresIn = 3600
        )

        every { authRepository.login("test@example.com", "password123") } returns Result.Success(expectedAuthResult)

        val result = loginUseCase("test@example.com", "password123")

        assertTrue(result is Result.Success)
        val success = result as Result.Success<AuthResult>
        assertEquals(expectedAuthResult, success.data)
        verify { authRepository.login("test@example.com", "password123") }
    }

    @Test
    fun login_failure_returnsError() = runBlocking {
        every { authRepository.login("wrong@example.com", "wrongpass") } returns Result.Error(Exception("Invalid credentials"), "Invalid credentials")

        val result = loginUseCase("wrong@example.com", "wrongpass")

        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertEquals("Invalid credentials", error.message)
    }

    @Test
    fun register_success_returnsAuthResult() = runBlocking {
        val expectedUser = User(
            id = "456",
            email = "new@example.com",
            displayName = "New User",
            photoUrl = null,
            isEmailVerified = false,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )
        val expectedAuthResult = AuthResult(
            user = expectedUser,
            accessToken = "new_access_token",
            refreshToken = "new_refresh_token",
            expiresIn = 3600
        )

        every { authRepository.register("new@example.com", "password123", "New User") } returns Result.Success(expectedAuthResult)

        val result = registerUseCase("new@example.com", "password123", "New User")

        assertTrue(result is Result.Success)
        val success = result as Result.Success<AuthResult>
        assertEquals(expectedAuthResult, success.data)
    }

    @Test
    fun logout_success_returnsUnit() = runBlocking {
        every { authRepository.logout() } returns Result.Success(Unit)

        val result = logoutUseCase()

        assertTrue(result is Result.Success)
        verify { authRepository.logout() }
    }

    @Test
    fun getCurrentUser_returnsUser() = runBlocking {
        val expectedUser = User(
            id = "123",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null,
            isEmailVerified = true,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )

        every { authRepository.getCurrentUser() } returns Result.Success(expectedUser)

        val result = getCurrentUserUseCase()

        assertTrue(result is Result.Success)
        val success = result as Result.Success<User?>
        assertEquals(expectedUser, success.data)
    }

    @Test
    fun sendPasswordReset_success_returnsUnit() = runBlocking {
        every { authRepository.sendPasswordResetEmail("test@example.com") } returns Result.Success(Unit)

        val result = sendPasswordResetUseCase("test@example.com")

        assertTrue(result is Result.Success)
        verify { authRepository.sendPasswordResetEmail("test@example.com") }
    }

    @Test
    fun updateProfile_success_returnsUpdatedUser() = runBlocking {
        val expectedUser = User(
            id = "123",
            email = "test@example.com",
            displayName = "Updated Name",
            photoUrl = "https://example.com/photo.jpg",
            isEmailVerified = true,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )

        every { authRepository.updateProfile("Updated Name", "https://example.com/photo.jpg") } returns Result.Success(expectedUser)

        val result = updateProfileUseCase("Updated Name", "https://example.com/photo.jpg")

        assertTrue(result is Result.Success)
        val success = result as Result.Success<User>
        assertEquals("Updated Name", success.data.displayName)
        assertEquals("https://example.com/photo.jpg", success.data.photoUrl)
    }
}