package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.AuthResult
import com.example.myapplication.domain.model.Result
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.repository.AuthRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AuthUseCasesTest {

    private val repository: AuthRepository = mockk()
    private val user = User(id = "1", email = "user@example.com", displayName = "Alex")
    private val authResult = AuthResult(user = user, accessToken = "token")

    private lateinit var login: LoginUseCase
    private lateinit var register: RegisterUseCase
    private lateinit var reset: SendPasswordResetUseCase
    private lateinit var updateProfile: UpdateProfileUseCase

    @Before
    fun setUp() {
        login = LoginUseCase(repository)
        register = RegisterUseCase(repository)
        reset = SendPasswordResetUseCase(repository)
        updateProfile = UpdateProfileUseCase(repository)
    }

    @Test
    fun `login with valid input delegates to repository`() = runTest {
        coEvery { repository.login("user@example.com", "secret1") } returns Result.Success(authResult)

        val result = login(" user@example.com ", "secret1")

        assertThat(result).isEqualTo(Result.Success(authResult))
        coVerify(exactly = 1) { repository.login("user@example.com", "secret1") }
    }

    @Test
    fun `login with invalid email fails without calling repository`() = runTest {
        val result = login("not-an-email", "secret1")

        assertThat(result).isInstanceOf(Result.Error::class.java)
        coVerify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun `login with blank password fails`() = runTest {
        val result = login("user@example.com", "")

        assertThat((result as Result.Error).message).isEqualTo("Password is required")
    }

    @Test
    fun `register rejects short password`() = runTest {
        val result = register("user@example.com", "123", "Alex", "123")

        assertThat(result).isInstanceOf(Result.Error::class.java)
        coVerify(exactly = 0) { repository.register(any(), any(), any()) }
    }

    @Test
    fun `register rejects mismatched confirmation`() = runTest {
        val result = register("user@example.com", "secret1", "Alex", "secret2")

        assertThat((result as Result.Error).message).isEqualTo("Passwords do not match")
    }

    @Test
    fun `register with valid input trims name and email`() = runTest {
        coEvery { repository.register("user@example.com", "secret1", "Alex") } returns Result.Success(authResult)

        val result = register(" user@example.com", "secret1", " Alex ", "secret1")

        assertThat(result).isEqualTo(Result.Success(authResult))
    }

    @Test
    fun `password reset validates email`() = runTest {
        val result = reset("bad")

        assertThat(result).isInstanceOf(Result.Error::class.java)
        coVerify(exactly = 0) { repository.sendPasswordResetEmail(any()) }
    }

    @Test
    fun `update profile converts blank photo url to null`() = runTest {
        coEvery { repository.updateProfile("Alex", null) } returns Result.Success(user)

        val result = updateProfile("Alex", "  ")

        assertThat(result).isEqualTo(Result.Success(user))
        coVerify { repository.updateProfile("Alex", null) }
    }
}
