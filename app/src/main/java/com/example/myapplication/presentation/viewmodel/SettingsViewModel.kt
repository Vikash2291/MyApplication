package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.preferences.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: kotlinx.coroutines.flow.StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<SettingsEvent?>(null)
    val events: kotlinx.coroutines.flow.StateFlow<SettingsEvent?> = _events.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val themeMode = preferencesRepository.themeMode.first()
            val notificationsEnabled = preferencesRepository.notificationsEnabled.first()
            val adConsent = preferencesRepository.adPersonalizationConsent.first()
            val gdprConsent = preferencesRepository.gdprConsent.first()
            val adMobEnabled = preferencesRepository.adMobEnabled.first()
            val unityAdsEnabled = preferencesRepository.unityAdsEnabled.first()
            val ironSourceEnabled = preferencesRepository.ironSourceEnabled.first()

            _uiState.update { it.copy(
                themeMode = themeMode,
                notificationsEnabled = notificationsEnabled,
                adPersonalizationConsent = adConsent,
                gdprConsent = gdprConsent,
                adMobEnabled = adMobEnabled,
                unityAdsEnabled = unityAdsEnabled,
                ironSourceEnabled = ironSourceEnabled
            ) }
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode).subscribe()
            _uiState.update { it.copy(themeMode = mode) }
            _events.value = SettingsEvent.ThemeChanged(mode)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationsEnabled(enabled).subscribe()
            _uiState.update { it.copy(notificationsEnabled = enabled) }
        }
    }

    fun setAdPersonalizationConsent(consent: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAdPersonalizationConsent(consent).subscribe()
            _uiState.update { it.copy(adPersonalizationConsent = consent) }
        }
    }

    fun setGdprConsent(consent: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setGdprConsent(consent).subscribe()
            _uiState.update { it.copy(gdprConsent = consent) }
        }
    }

    fun setAdMobEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAdMobEnabled(enabled).subscribe()
            _uiState.update { it.copy(adMobEnabled = enabled) }
        }
    }

    fun setUnityAdsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setUnityAdsEnabled(enabled).subscribe()
            _uiState.update { it.copy(unityAdsEnabled = enabled) }
        }
    }

    fun setIronSourceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setIronSourceEnabled(enabled).subscribe()
            _uiState.update { it.copy(ironSourceEnabled = enabled) }
        }
    }

    fun clearData() {
        viewModelScope.launch {
            preferencesRepository.clearAll().subscribe()
            _uiState.update { SettingsUiState() }
            _events.value = SettingsEvent.DataCleared
        }
    }

    fun consumeEvent(): SettingsEvent? {
        return _events.value?.also { _events.value = null }
    }
}

data class SettingsUiState(
    val themeMode: Int = 0, // 0 = system, 1 = light, 2 = dark
    val notificationsEnabled: Boolean = true,
    val adPersonalizationConsent: Boolean = false,
    val gdprConsent: Boolean = false,
    val adMobEnabled: Boolean = true,
    val unityAdsEnabled: Boolean = true,
    val ironSourceEnabled: Boolean = true
)

sealed interface SettingsEvent {
    data class ThemeChanged(val mode: Int) : SettingsEvent
    data class DataCleared : SettingsEvent
}