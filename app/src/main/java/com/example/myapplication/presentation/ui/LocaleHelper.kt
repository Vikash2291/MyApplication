package com.example.myapplication.presentation.ui

import android.content.Context
import android.content.res.Configuration
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.domain.quiz.AppLanguage
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

/**
 * Applies the in-app language (stored in DataStore) to an Activity's base context, so every
 * stringResource / getString call resolves values/ or values-hi/ accordingly.
 */
object LocaleHelper {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PreferencesEntryPoint {
        fun preferences(): PreferencesRepository
    }

    /** Reads the saved language synchronously; called once from attachBaseContext. */
    fun savedLanguage(context: Context): AppLanguage = runCatching {
        val prefs = EntryPointAccessors
            .fromApplication(context.applicationContext, PreferencesEntryPoint::class.java)
            .preferences()
        runBlocking { prefs.settings.first().language }
    }.getOrDefault(AppLanguage.ENGLISH)

    fun wrap(base: Context, language: AppLanguage): Context {
        val locale = Locale.forLanguageTag(language.tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }
}
