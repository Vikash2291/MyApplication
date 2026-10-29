package com.example.myapplication.data.local.preferences

import com.example.myapplication.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/** Snapshot of every user-facing setting, emitted as a single stream. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val gdprConsent: Boolean = false,
    val adMobEnabled: Boolean = true,
    val rewardPoints: Int = 0,
    val lastSyncTime: Long = 0L
)

interface PreferencesRepository {
    val settings: Flow<AppSettings>
    val isFirstLaunch: Flow<Boolean>
    val authToken: Flow<String?>

    suspend fun setFirstLaunchComplete()
    suspend fun saveAuthToken(token: String?)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setGdprConsent(consent: Boolean)
    suspend fun setAdMobEnabled(enabled: Boolean)
    suspend fun addRewardPoints(points: Int)
    suspend fun updateLastSyncTime(time: Long = System.currentTimeMillis())

    /** Clears session data (token) but keeps device settings. */
    suspend fun clearSession()

    /** Clears everything, restoring defaults. */
    suspend fun clearAll()
}
