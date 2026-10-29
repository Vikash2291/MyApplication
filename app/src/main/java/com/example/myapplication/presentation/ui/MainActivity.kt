package com.example.myapplication.presentation.ui

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.ads.AdsManager
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.domain.quiz.AppLanguage
import com.example.myapplication.presentation.navigation.AppNavHost
import com.example.myapplication.presentation.navigation.Screen
import com.example.myapplication.presentation.ui.components.LoadingScreen
import com.example.myapplication.presentation.ui.theme.AppTheme
import com.example.myapplication.presentation.viewmodel.MainUiState
import com.example.myapplication.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var adsManager: AdsManager
    @Inject lateinit var preferencesRepository: PreferencesRepository

    /** Language the current Activity instance was created with. */
    private var appliedLanguage: AppLanguage = AppLanguage.ENGLISH

    override fun attachBaseContext(newBase: Context) {
        appliedLanguage = LocaleHelper.savedLanguage(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, appliedLanguage))
    }

    private val viewModel: MainViewModel by viewModels()

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        adsManager.gatherConsentAndInitialize(this)
        requestNotificationPermissionIfNeeded()

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val settings = (state as? MainUiState.Ready)?.settings

            // Language toggled in Settings: recreate so every screen picks up values-hi / values.
            val language = settings?.language
            LaunchedEffect(language) {
                if (language != null && language != appliedLanguage) recreate()
            }

            AppTheme(
                themeMode = settings?.themeMode ?: com.example.myapplication.domain.model.ThemeMode.SYSTEM,
                dynamicColor = settings?.dynamicColor ?: true
            ) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when (val s = state) {
                        MainUiState.Loading -> LoadingScreen()
                        is MainUiState.Ready -> {
                            val navController = rememberNavController()
                            // Decide the start screen once; later auth changes navigate via events.
                            val start = remember {
                                if (s.canEnterApp) Screen.Home.route else Screen.Login.route
                            }
                            AppNavHost(
                                navController = navController,
                                startDestination = start,
                                onContinueAsGuest = {
                                    lifecycleScope.launch { preferencesRepository.setGuestMode(true) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        const val EXTRA_DEEPLINK = "extra_deeplink"
    }
}
