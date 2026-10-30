package com.example.myapplication.data.repository

import com.example.myapplication.data.auth.AuthDataSource
import com.example.myapplication.data.local.database.dao.SettingsDao
import com.example.myapplication.data.local.database.dao.UserDao
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.data.remote.ApiService
import com.example.myapplication.domain.model.ReauthenticationRequiredException
import com.example.myapplication.domain.model.Result
import com.example.myapplication.domain.quiz.QuizRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class UserRepositoryImplTest {

    private val authDataSource: AuthDataSource = mockk(relaxed = true)
    private val apiService: ApiService = mockk(relaxed = true)
    private val userDao: UserDao = mockk(relaxed = true)
    private val settingsDao: SettingsDao = mockk(relaxed = true)
    private val preferences: PreferencesRepository = mockk(relaxed = true)
    private val quizRepository: QuizRepository = mockk(relaxed = true)

    private val repository = UserRepositoryImpl(authDataSource, apiService, userDao, settingsDao, preferences, quizRepository)

    @Test
    fun `deleteAccount deletes user then wipes Room and DataStore and signs out`() = runTest {
        coEvery { apiService.deleteAccount() } throws IOException("offline")

        val result = repository.deleteAccount()

        assertThat(result).isEqualTo(Result.Success(Unit))
        coVerifyOrder {
            authDataSource.deleteUser()
            userDao.clearAllUsers()
            settingsDao.clearAllSettings()
            quizRepository.clearUserData()
            preferences.clearAll()
        }
        verify { authDataSource.signOut() }
        coVerify(exactly = 0) { authDataSource.reauthenticate(any()) }
    }

    @Test
    fun `requires-recent-login keeps local data and surfaces reauth exception`() = runTest {
        coEvery { authDataSource.deleteUser() } throws ReauthenticationRequiredException()

        val result = repository.deleteAccount()

        assertThat((result as Result.Error).exception).isInstanceOf(ReauthenticationRequiredException::class.java)
        coVerify(exactly = 0) { userDao.clearAllUsers() }
        coVerify(exactly = 0) { preferences.clearAll() }
        coVerify(exactly = 0) { quizRepository.clearUserData() }
        verify(exactly = 0) { authDataSource.signOut() }
    }

    @Test
    fun `password is used to reauthenticate before deleting`() = runTest {
        val result = repository.deleteAccount(password = "secret1")

        assertThat(result).isEqualTo(Result.Success(Unit))
        coVerifyOrder {
            authDataSource.reauthenticate("secret1")
            authDataSource.deleteUser()
        }
    }
}
