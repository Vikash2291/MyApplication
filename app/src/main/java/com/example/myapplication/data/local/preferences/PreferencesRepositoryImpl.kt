package com.example.myapplication.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.core.common.Constants
import com.example.myapplication.domain.model.ThemeMode
import com.example.myapplication.domain.quiz.AppLanguage
import com.example.myapplication.domain.quiz.StreakCalculator
import com.example.myapplication.domain.quiz.StreakState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(Constants.DATASTORE_NAME)

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : PreferencesRepository {

    private val dataStore = context.dataStore

    private object Keys {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val GDPR_CONSENT = booleanPreferencesKey("gdpr_consent")
        val ADMOB_ENABLED = booleanPreferencesKey("admob_enabled")
        val REWARD_POINTS = intPreferencesKey("reward_points")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val LANGUAGE = stringPreferencesKey("app_language")
        val STREAK_CURRENT = intPreferencesKey("daily_streak_current")
        val STREAK_BEST = intPreferencesKey("daily_streak_best")
        val STREAK_LAST_DAY = longPreferencesKey("daily_streak_last_epoch_day")
        val QUESTION_BANK_VERSION = intPreferencesKey("question_bank_version")
        val GUEST_MODE = booleanPreferencesKey("guest_mode")
    }

    private val data: Flow<Preferences> = dataStore.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    override val settings: Flow<AppSettings> = data.map { p ->
        AppSettings(
            themeMode = p[Keys.THEME_MODE]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            dynamicColor = p[Keys.DYNAMIC_COLOR] ?: true,
            notificationsEnabled = p[Keys.NOTIFICATIONS_ENABLED] ?: true,
            gdprConsent = p[Keys.GDPR_CONSENT] ?: false,
            adMobEnabled = p[Keys.ADMOB_ENABLED] ?: true,
            rewardPoints = p[Keys.REWARD_POINTS] ?: 0,
            lastSyncTime = p[Keys.LAST_SYNC_TIME] ?: 0L,
            language = AppLanguage.fromTag(p[Keys.LANGUAGE]),
            streak = p.streak(),
            guestMode = p[Keys.GUEST_MODE] ?: false
        )
    }.distinctUntilChanged()

    override val isFirstLaunch: Flow<Boolean> = data.map { it[Keys.IS_FIRST_LAUNCH] ?: true }

    override val authToken: Flow<String?> = data.map { it[Keys.AUTH_TOKEN] }

    override suspend fun setFirstLaunchComplete() {
        dataStore.edit { it[Keys.IS_FIRST_LAUNCH] = false }
    }

    override suspend fun saveAuthToken(token: String?) {
        dataStore.edit {
            if (token.isNullOrEmpty()) it.remove(Keys.AUTH_TOKEN) else it[Keys.AUTH_TOKEN] = token
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun setGdprConsent(consent: Boolean) {
        dataStore.edit { it[Keys.GDPR_CONSENT] = consent }
    }

    override suspend fun setAdMobEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.ADMOB_ENABLED] = enabled }
    }

    override suspend fun addRewardPoints(points: Int) {
        dataStore.edit { it[Keys.REWARD_POINTS] = (it[Keys.REWARD_POINTS] ?: 0) + points }
    }

    override suspend fun updateLastSyncTime(time: Long) {
        dataStore.edit { it[Keys.LAST_SYNC_TIME] = time }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { it[Keys.LANGUAGE] = language.tag }
    }

    override suspend fun setGuestMode(enabled: Boolean) {
        dataStore.edit { it[Keys.GUEST_MODE] = enabled }
    }

    override suspend fun recordDailyQuizCompleted(epochDay: Long): StreakState {
        var result = StreakState()
        dataStore.edit { prefs ->
            val updated = StreakCalculator.onDailyCompleted(prefs.streak(), epochDay)
            prefs[Keys.STREAK_CURRENT] = updated.current
            prefs[Keys.STREAK_BEST] = updated.best
            updated.lastCompletedEpochDay?.let { prefs[Keys.STREAK_LAST_DAY] = it }
            result = updated
        }
        return result
    }

    override suspend fun getQuestionBankVersion(): Int = data.first()[Keys.QUESTION_BANK_VERSION] ?: 0

    override suspend fun setQuestionBankVersion(version: Int) {
        dataStore.edit { it[Keys.QUESTION_BANK_VERSION] = version }
    }

    private fun Preferences.streak(): StreakState = StreakState(
        current = this[Keys.STREAK_CURRENT] ?: 0,
        best = this[Keys.STREAK_BEST] ?: 0,
        lastCompletedEpochDay = this[Keys.STREAK_LAST_DAY]
    )

    override suspend fun clearSession() {
        dataStore.edit { it.remove(Keys.AUTH_TOKEN) }
    }

    override suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
