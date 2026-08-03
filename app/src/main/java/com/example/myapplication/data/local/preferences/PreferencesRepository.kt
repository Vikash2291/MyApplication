package com.example.myapplication.data.local.preferences

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

interface PreferencesRepository {
    val isFirstLaunch: Single<Boolean>
    fun setFirstLaunchComplete(): Completable

    val userId: Maybe<String>
    fun saveUserId(userId: String): Completable

    val userEmail: Maybe<String>
    fun saveUserEmail(email: String): Completable

    val userDisplayName: Maybe<String>
    fun saveUserDisplayName(displayName: String): Completable

    val userPhotoUrl: Maybe<String>
    fun saveUserPhotoUrl(photoUrl: String): Completable

    val isLoggedIn: Single<Boolean>
    fun setLoggedIn(loggedIn: Boolean): Completable

    val themeMode: Single<Int>
    fun setThemeMode(mode: Int): Completable

    val notificationsEnabled: Single<Boolean>
    fun setNotificationsEnabled(enabled: Boolean): Completable

    val adPersonalizationConsent: Single<Boolean>
    fun setAdPersonalizationConsent(consent: Boolean): Completable

    val gdprConsent: Single<Boolean>
    fun setGdprConsent(consent: Boolean): Completable

    // Ad Network Toggles
    val adMobEnabled: Single<Boolean>
    fun setAdMobEnabled(enabled: Boolean): Completable

    val unityAdsEnabled: Single<Boolean>
    fun setUnityAdsEnabled(enabled: Boolean): Completable

    val ironSourceEnabled: Single<Boolean>
    fun setIronSourceEnabled(enabled: Boolean): Completable

    val appLanguage: Maybe<String>
    fun setAppLanguage(language: String): Completable

    val lastSyncTime: Single<Long>
    fun updateLastSyncTime(): Completable

    val buildVersion: Single<Int>
    fun setBuildVersion(version: Int): Completable

    fun clearAll(): Completable
}