package com.example.myapplication.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.rxjava2.preferencesDataStore
import com.example.myapplication.data.local.preferences.PreferencesRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val context: Context
) : PreferencesRepository {

    private val dataStore = context.preferencesDataStore("app_preferences")

    private object Keys {
        val IS_FIRST_LAUNCH = PreferencesKeys.boolean("is_first_launch")
        val USER_ID = PreferencesKeys.string("user_id")
        val USER_EMAIL = PreferencesKeys.string("user_email")
        val USER_DISPLAY_NAME = PreferencesKeys.string("user_display_name")
        val USER_PHOTO_URL = PreferencesKeys.string("user_photo_url")
        val IS_LOGGED_IN = PreferencesKeys.boolean("is_logged_in")
        val THEME_MODE = PreferencesKeys.int("theme_mode")
        val NOTIFICATIONS_ENABLED = PreferencesKeys.boolean("notifications_enabled")
        val AD_PERSONALIZATION_CONSENT = PreferencesKeys.boolean("ad_personalization_consent")
        val GDPR_CONSENT = PreferencesKeys.boolean("gdpr_consent")
        // Ad Network Toggles
        val ADMOB_ENABLED = PreferencesKeys.boolean("admob_enabled")
        val UNITY_ADS_ENABLED = PreferencesKeys.boolean("unity_ads_enabled")
        val IRON_SOURCE_ENABLED = PreferencesKeys.boolean("iron_source_enabled")
        val APP_LANGUAGE = PreferencesKeys.string("app_language")
        val LAST_SYNC_TIME = PreferencesKeys.long("last_sync_time")
        val BUILD_VERSION = PreferencesKeys.int("build_version")
    }

    override val isFirstLaunch: Single<Boolean> = dataStore.data
        .map { it[Keys.IS_FIRST_LAUNCH] ?: true }
        .firstElement()

    override fun setFirstLaunchComplete(): Completable = dataStore.edit { it[Keys.IS_FIRST_LAUNCH] = false }.ignoreElement()

    override val userId: Maybe<String> = dataStore.data
        .map { it[Keys.USER_ID] }
        .firstElement()

    override fun saveUserId(userId: String): Completable = dataStore.edit { it[Keys.USER_ID] = userId }.ignoreElement()

    override val userEmail: Maybe<String> = dataStore.data
        .map { it[Keys.USER_EMAIL] }
        .firstElement()

    override fun saveUserEmail(email: String): Completable = dataStore.edit { it[Keys.USER_EMAIL] = email }.ignoreElement()

    override val userDisplayName: Maybe<String> = dataStore.data
        .map { it[Keys.USER_DISPLAY_NAME] }
        .firstElement()

    override fun saveUserDisplayName(displayName: String): Completable = dataStore.edit { it[Keys.USER_DISPLAY_NAME] = displayName }.ignoreElement()

    override val userPhotoUrl: Maybe<String> = dataStore.data
        .map { it[Keys.USER_PHOTO_URL] }
        .firstElement()

    override fun saveUserPhotoUrl(photoUrl: String): Completable = dataStore.edit { it[Keys.USER_PHOTO_URL] = photoUrl }.ignoreElement()

    override val isLoggedIn: Single<Boolean> = dataStore.data
        .map { it[Keys.IS_LOGGED_IN] ?: false }
        .firstElement()

    override fun setLoggedIn(loggedIn: Boolean): Completable = dataStore.edit { it[Keys.IS_LOGGED_IN] = loggedIn }.ignoreElement()

    override val themeMode: Single<Int> = dataStore.data
        .map { it[Keys.THEME_MODE] ?: 0 }
        .firstElement()

    override fun setThemeMode(mode: Int): Completable = dataStore.edit { it[Keys.THEME_MODE] = mode }.ignoreElement()

    override val notificationsEnabled: Single<Boolean> = dataStore.data
        .map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }
        .firstElement()

    override fun setNotificationsEnabled(enabled: Boolean): Completable = dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }.ignoreElement()

    override val adPersonalizationConsent: Single<Boolean> = dataStore.data
        .map { it[Keys.AD_PERSONALIZATION_CONSENT] ?: false }
        .firstElement()

    override fun setAdPersonalizationConsent(consent: Boolean): Completable = dataStore.edit { it[Keys.AD_PERSONALIZATION_CONSENT] = consent }.ignoreElement()

    override val gdprConsent: Single<Boolean> = dataStore.data
        .map { it[Keys.GDPR_CONSENT] ?: false }
        .firstElement()

    override fun setGdprConsent(consent: Boolean): Completable = dataStore.edit { it[Keys.GDPR_CONSENT] = consent }.ignoreElement()

    // Ad Network Toggles
    override val adMobEnabled: Single<Boolean> = dataStore.data
        .map { it[Keys.ADMOB_ENABLED] ?: true }
        .firstElement()

    override fun setAdMobEnabled(enabled: Boolean): Completable = dataStore.edit { it[Keys.ADMOB_ENABLED] = enabled }.ignoreElement()

    override val unityAdsEnabled: Single<Boolean> = dataStore.data
        .map { it[Keys.UNITY_ADS_ENABLED] ?: true }
        .firstElement()

    override fun setUnityAdsEnabled(enabled: Boolean): Completable = dataStore.edit { it[Keys.UNITY_ADS_ENABLED] = enabled }.ignoreElement()

    override val ironSourceEnabled: Single<Boolean> = dataStore.data
        .map { it[Keys.IRON_SOURCE_ENABLED] ?: true }
        .firstElement()

    override fun setIronSourceEnabled(enabled: Boolean): Completable = dataStore.edit { it[Keys.IRON_SOURCE_ENABLED] = enabled }.ignoreElement()

    override val appLanguage: Maybe<String> = dataStore.data
        .map { it[Keys.APP_LANGUAGE] }
        .firstElement()

    override fun setAppLanguage(language: String): Completable = dataStore.edit { it[Keys.APP_LANGUAGE] = language }.ignoreElement()

    override val lastSyncTime: Single<Long> = dataStore.data
        .map { it[Keys.LAST_SYNC_TIME] ?: 0L }
        .firstElement()

    override fun updateLastSyncTime(): Completable = dataStore.edit { it[Keys.LAST_SYNC_TIME] = System.currentTimeMillis() }.ignoreElement()

    override val buildVersion: Single<Int> = dataStore.data
        .map { it[Keys.BUILD_VERSION] ?: 0 }
        .firstElement()

    override fun setBuildVersion(version: Int): Completable = dataStore.edit { it[Keys.BUILD_VERSION] = version }.ignoreElement()

    override fun clearAll(): Completable = dataStore.edit { it.clear() }.ignoreElement()
}