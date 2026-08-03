package com.example.myapplication.presentation.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.presentation.navigation.Screen
import com.example.myapplication.presentation.navigation.AppNavigator
import com.example.myapplication.presentation.ui.theme.AppTheme
import com.example.myapplication.presentation.ui.screens.HomeScreen
import com.example.myapplication.presentation.ui.screens.LoginScreen
import com.example.myapplication.presentation.ui.screens.RegisterScreen
import com.example.myapplication.presentation.ui.screens.ForgotPasswordScreen
import com.example.myapplication.presentation.ui.screens.SettingsScreen
import com.example.myapplication.presentation.ui.screens.ProfileScreen
import com.example.myapplication.presentation.viewmodel.AuthViewModel
import com.example.myapplication.presentation.viewmodel.HomeViewModel
import com.example.myapplication.presentation.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private lateinit var appNavigator: AppNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MaterialTheme {
                    Surface(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        appNavigator = AppNavigator(navController)
                        AppNavHost(navController, appNavigator)
                    }
                }
            }
        }

        // Handle deep link from SplashActivity
        intent.getStringExtra("navigate_to")?.let { destination ->
            when (destination) {
                "login" -> {
                    // Navigation will be handled by observing auth state
                }
            }
        }

        // Observe auth state for navigation
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.uiState
                    .distinctUntilChanged { a, b -> a.isLoggedIn == b.isLoggedIn }
                    .collect { state ->
                        if (!state.isLoggedIn && !isLoginDestination(navController.currentBackStackEntryAsState().value?.destination?.route)) {
                            appNavigator.navigateTo(Screen.Login)
                        }
                    }
            }
        }
    }

    private fun isLoginDestination(route: String?): Boolean {
        return route?.startsWith(Screen.Login.route) == true || 
               route?.startsWith(Screen.Register.route) == true || 
               route?.startsWith(Screen.ForgotPassword.route) == true
    }

    private lateinit var navController: NavHostController
}

@Composable
fun AppNavHost(navController: NavHostController, appNavigator: AppNavigator) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen()
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.login(email, password)
                },
                onRegisterClick = {
                    appNavigator.navigateTo(Screen.Register)
                },
                onForgotPasswordClick = {
                    appNavigator.navigateTo(Screen.ForgotPassword)
                },
                onAuthEvent = { event ->
                    when (event) {
                        is com.example.myapplication.presentation.viewmodel.AuthEvent.NavigateToHome -> {
                            appNavigator.clearBackStack()
                            appNavigator.navigateTo(Screen.Home)
                        }
                        is com.example.myapplication.presentation.viewmodel.AuthEvent.ShowMessage -> {
                            // Show snackbar
                        }
                    }
                },
                uiState = authViewModel.uiState
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterClick = { email, password, displayName ->
                    authViewModel.register(email, password, displayName)
                },
                onLoginClick = {
                    appNavigator.navigateTo(Screen.Login)
                },
                onAuthEvent = { event ->
                    when (event) {
                        is com.example.myapplication.presentation.viewmodel.AuthEvent.NavigateToHome -> {
                            appNavigator.clearBackStack()
                            appNavigator.navigateTo(Screen.Home)
                        }
                        is com.example.myapplication.presentation.viewmodel.AuthEvent.ShowMessage -> {
                            // Show snackbar
                        }
                    }
                },
                uiState = authViewModel.uiState
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onResetClick = { email ->
                    authViewModel.sendPasswordReset(email)
                },
                onBackClick = {
                    appNavigator.navigateTo(Screen.Login)
                },
                uiState = authViewModel.uiState
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onRewardedAdClick = {
                    homeViewModel.showRewardedAd()
                },
                onInterstitialAdClick = {
                    homeViewModel.showInterstitialAd()
                },
                onSettingsClick = {
                    appNavigator.navigateTo(Screen.Settings)
                },
                onProfileClick = {
                    appNavigator.navigateTo(Screen.Profile)
                },
                onHomeEvent = { event ->
                    when (event) {
                        is com.example.myapplication.presentation.viewmodel.HomeEvent.RewardGranted -> {
                            // Show reward granted
                        }
                        is com.example.myapplication.presentation.viewmodel.HomeEvent.ShowMessage -> {
                            // Show snackbar
                        }
                        is com.example.myapplication.presentation.viewmodel.HomeEvent.NavigateToSettings -> {
                            appNavigator.navigateTo(Screen.Settings)
                        }
                    }
                },
                uiState = homeViewModel.uiState
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onThemeChange = { mode ->
                    settingsViewModel.setThemeMode(mode)
                },
                onNotificationsChange = { enabled ->
                    settingsViewModel.setNotificationsEnabled(enabled)
                },
                onAdConsentChange = { consent ->
                    settingsViewModel.setAdPersonalizationConsent(consent)
                },
                onGdprConsentChange = { consent ->
                    settingsViewModel.setGdprConsent(consent)
                },
                onAdMobChange = { enabled ->
                    settingsViewModel.setAdMobEnabled(enabled)
                },
                onUnityAdsChange = { enabled ->
                    settingsViewModel.setUnityAdsEnabled(enabled)
                },
                onIronSourceChange = { enabled ->
                    settingsViewModel.setIronSourceEnabled(enabled)
                },
                onClearData = {
                    settingsViewModel.clearData()
                },
                onBackClick = {
                    appNavigator.navigateUp()
                },
                uiState = settingsViewModel.uiState
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onUpdateProfile = { displayName, photoUrl ->
                    authViewModel.updateProfile(displayName, photoUrl)
                },
                onLogoutClick = {
                    authViewModel.logout()
                },
                onBackClick = {
                    appNavigator.navigateUp()
                },
                uiState = authViewModel.uiState
            )
        }
    }
}

@Composable
fun SplashScreen() {
    com.example.myapplication.presentation.ui.components.LoadingScreen(message = "Loading...")
}