package com.example.myapplication.presentation.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.presentation.ui.components.rememberActivity
import com.example.myapplication.presentation.ui.screens.AboutScreen
import com.example.myapplication.presentation.ui.screens.ForgotPasswordScreen
import com.example.myapplication.presentation.ui.screens.HomeScreen
import com.example.myapplication.presentation.ui.screens.LoginScreen
import com.example.myapplication.presentation.ui.screens.ProfileScreen
import com.example.myapplication.presentation.ui.screens.RegisterScreen
import com.example.myapplication.presentation.ui.screens.SettingsScreen
import com.example.myapplication.presentation.viewmodel.AuthEvent
import com.example.myapplication.presentation.viewmodel.AuthViewModel
import com.example.myapplication.presentation.viewmodel.HomeEvent
import com.example.myapplication.presentation.viewmodel.HomeViewModel
import com.example.myapplication.presentation.viewmodel.SettingsEvent
import com.example.myapplication.presentation.viewmodel.SettingsViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String
) {
    // Shared across auth/profile screens so state survives navigation between them.
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    fun goHome() = navController.navigate(Screen.Home.route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }

    fun goLogin() = navController.navigate(Screen.Login.route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }

    LaunchedEffect(authViewModel) {
        authViewModel.events.collect { event ->
            when (event) {
                AuthEvent.NavigateToHome -> goHome()
                AuthEvent.NavigateToLogin -> goLogin()
                is AuthEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                uiState = authState,
                onLoginClick = authViewModel::login,
                onRegisterClick = {
                    authViewModel.clearError()
                    navController.navigate(Screen.Register.route)
                },
                onForgotPasswordClick = {
                    authViewModel.clearError()
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onInputChanged = { if (authState.error != null) authViewModel.clearError() }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                uiState = authState,
                onRegisterClick = { name, email, password, confirm ->
                    authViewModel.register(email, password, confirm, name)
                },
                onLoginClick = {
                    authViewModel.clearError()
                    navController.popBackStack()
                },
                onInputChanged = { if (authState.error != null) authViewModel.clearError() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                uiState = authState,
                onResetClick = authViewModel::sendPasswordReset,
                onBackClick = {
                    authViewModel.clearError()
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
            val activity = rememberActivity()

            LaunchedEffect(homeViewModel) {
                homeViewModel.events.collect { event ->
                    when (event) {
                        is HomeEvent.RewardGranted ->
                            snackbarHostState.showSnackbar("You earned ${event.amount} points!")
                        is HomeEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                    }
                }
            }

            // Interstitials only at a natural break after a user action (leaving Home for a
            // secondary screen). AdsManager enforces: >= 2 user actions between interstitials,
            // >= 2 minutes apart, never on launch/exit, never over another full-screen ad.
            fun navigateWithBreak(route: String) {
                if (activity != null) {
                    homeViewModel.onNaturalBreak(activity) { navController.navigate(route) }
                } else {
                    navController.navigate(route)
                }
            }

            HomeScreen(
                uiState = homeState,
                snackbarHostState = snackbarHostState,
                onWatchRewardedAd = { activity?.let(homeViewModel::showRewardedAd) },
                onProfileClick = { navigateWithBreak(Screen.Profile.route) },
                onSettingsClick = { navigateWithBreak(Screen.Settings.route) },
                onAboutClick = { navigateWithBreak(Screen.About.route) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                uiState = authState,
                snackbarHostState = snackbarHostState,
                onSaveProfile = { name, photo -> authViewModel.updateProfile(name, photo) },
                onVerifyEmail = authViewModel::sendEmailVerification,
                onLogout = authViewModel::logout,
                onDeleteAccount = { password -> authViewModel.deleteAccount(password) },
                onCancelReauth = authViewModel::cancelReauth,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val activity = rememberActivity()

            LaunchedEffect(settingsViewModel) {
                settingsViewModel.events.collect { event ->
                    when (event) {
                        SettingsEvent.DataCleared -> snackbarHostState.showSnackbar("Settings reset")
                        is SettingsEvent.Message -> snackbarHostState.showSnackbar(event.text)
                    }
                }
            }

            SettingsScreen(
                uiState = settingsState,
                snackbarHostState = snackbarHostState,
                onThemeChange = settingsViewModel::setThemeMode,
                onDynamicColorChange = settingsViewModel::setDynamicColor,
                onNotificationsChange = settingsViewModel::setNotificationsEnabled,
                onAdMobChange = settingsViewModel::setAdMobEnabled,
                onPrivacyOptionsClick = { activity?.let(settingsViewModel::openPrivacyOptions) },
                onClearData = settingsViewModel::clearData,
                onDeleteAccountClick = { navController.navigate(Screen.Profile.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
