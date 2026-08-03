package com.example.myapplication.presentation.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.myapplication.presentation.ui.theme.AppTheme
import com.example.myapplication.presentation.ui.screens.SettingsScreen
import com.example.myapplication.presentation.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                SettingsScreen(
                    onThemeChange = { mode -> viewModel.setThemeMode(mode) },
                    onNotificationsChange = { enabled -> viewModel.setNotificationsEnabled(enabled) },
                    onAdConsentChange = { consent -> viewModel.setAdPersonalizationConsent(consent) },
                    onGdprConsentChange = { consent -> viewModel.setGdprConsent(consent) },
                    onClearData = { viewModel.clearData() },
                    onBackClick = { finish() },
                    uiState = viewModel.uiState
                )
            }
        }
    }
}