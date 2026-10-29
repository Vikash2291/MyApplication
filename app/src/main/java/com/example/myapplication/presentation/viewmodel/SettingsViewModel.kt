package com.example.myapplication.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ads.AdsManager
import com.example.myapplication.data.local.preferences.AppSettings
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val isPrivacyOptionsRequired: Boolean = false
)

sealed interface SettingsEvent {
    data object DataCleared : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val adsManager: AdsManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.settings,
        adsManager.state
    ) { settings, ads ->
        SettingsUiState(settings, ads.isPrivacyOptionsRequired)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events: Flow<SettingsEvent> = _events.receiveAsFlow()

    fun setThemeMode(mode: ThemeMode) = save { setThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) = save { setDynamicColor(enabled) }
    fun setNotificationsEnabled(enabled: Boolean) = save { setNotificationsEnabled(enabled) }
    fun setAdPersonalizationConsent(consent: Boolean) = save { setAdPersonalizationConsent(consent) }
    fun setAdMobEnabled(enabled: Boolean) = save { setAdMobEnabled(enabled) }
    fun setUnityAdsEnabled(enabled: Boolean) = save { setUnityAdsEnabled(enabled) }

    fun openPrivacyOptions(activity: Activity) = adsManager.showPrivacyOptionsForm(activity)

    fun clearData() {
        viewModelScope.launch {
            preferencesRepository.clearAll()
            _events.send(SettingsEvent.DataCleared)
        }
    }

    private fun save(block: suspend PreferencesRepository.() -> Unit) {
        viewModelScope.launch { preferencesRepository.block() }
    }
}
