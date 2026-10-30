package com.example.myapplication.data.repository

import com.example.myapplication.data.auth.AuthDataSource
import com.example.myapplication.data.auth.AuthNotConfiguredException
import com.example.myapplication.data.device.DeviceInfoProvider
import com.example.myapplication.data.local.database.dao.UserDao
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.data.remote.ApiService
import com.example.myapplication.domain.model.Result
import com.example.myapplication.domain.model.User
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AuthRepositoryImplTest {

    private val authDataSource: AuthDataSource = mockk(relaxed = true)
    private val apiService: ApiService = mockk()
    private val userDao: UserDao = mockk(relaxed = true)
    private val preferences: PreferencesRepository = mockk(relaxed = true)
    private val deviceInfo: DeviceInfoProvider = mockk {
        every { deviceId } returns "device-1"
        every { deviceName } returns "Pixel"
    }

    private val user = User(id = "uid", email = "user@example.com", displayName = "Alex")
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        every { authDataSource.authState } returns flowOf(null)
        repository = AuthRepositoryImpl(authDataSource, apiService, userDao, preferences, deviceInfo)
    }

    @Test
    fun `login succeeds even when backend is unreachable`() = runTest {
        coEvery { authDataSource.signIn("user@example.com", "secret1") } returns user
        coEvery { authDataSource.idToken(true) } returns "id-token"
        coEvery { apiService.syncSession(any()) } throws IOException("offline")

        val result = repository.login("user@example.com", "secret1")

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data.accessToken).isEqualTo("id-token")
        coVerify { preferences.saveAuthToken("id-token") }
        coVerify { userDao.insertUser(match { it.id == "uid" && it.accessToken == "id-token" }) }
    }

    @Test
    fun `login maps provider failure to error`() = runTest {
        coEvery { authDataSource.signIn(any(), any()) } throws IllegalStateException("Wrong password")

        val result = repository.login("user@example.com", "bad")

        assertThat((result as Result.Error).message).isEqualTo("Wrong password")
    }

    @Test
    fun `missing firebase config returns helpful error`() = runTest {
        coEvery { authDataSource.signIn(any(), any()) } throws AuthNotConfiguredException()

        val result = repository.login("user@example.com", "secret1")

        assertThat((result as Result.Error).message).contains("google-services.json")
    }

    @Test
    fun `logout clears local session`() = runTest {
        coEvery { apiService.logout() } throws IOException("offline")
        every { authDataSource.signOut() } just runs

        val result = repository.logout()

        assertThat(result).isEqualTo(Result.Success(Unit))
        verify { authDataSource.signOut() }
        coVerify { userDao.clearAllUsers() }
        coVerify { preferences.clearSession() }
    }

    @Test
    fun `getCurrentUser returns null when signed out`() = runTest {
        every { authDataSource.currentUser() } returns null

        assertThat(repository.getCurrentUser()).isEqualTo(Result.Success(null))
    }
}
