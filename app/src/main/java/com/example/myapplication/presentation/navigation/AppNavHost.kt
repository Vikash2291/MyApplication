package com.example.myapplication.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHostState
import com.example.myapplication.presentation.viewmodel.BannerViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.R
import com.example.myapplication.domain.quiz.QuizCategory
import com.example.myapplication.domain.quiz.QuizMode
import com.example.myapplication.presentation.ui.components.rememberActivity
import com.example.myapplication.presentation.ui.screens.AboutScreen
import com.example.myapplication.presentation.ui.screens.BookmarksScreen
import com.example.myapplication.presentation.ui.screens.ForgotPasswordScreen
import com.example.myapplication.presentation.ui.screens.HomeScreen
import com.example.myapplication.presentation.ui.screens.LoginScreen
import com.example.myapplication.presentation.ui.screens.ProfileScreen
import com.example.myapplication.presentation.ui.screens.QuizScreen
import com.example.myapplication.presentation.ui.screens.RegisterScreen
import com.example.myapplication.presentation.ui.screens.ResultScreen
import com.example.myapplication.presentation.ui.screens.SettingsScreen
import com.example.myapplication.presentation.ui.screens.StatsScreen
import com.example.myapplication.presentation.viewmodel.AuthEvent
import com.example.myapplication.presentation.viewmodel.AuthViewModel
import com.example.myapplication.presentation.viewmodel.BookmarksViewModel
import com.example.myapplication.presentation.viewmodel.HomeEvent
import com.example.myapplication.presentation.viewmodel.HomeViewModel
import com.example.myapplication.presentation.viewmodel.QuizEvent
import com.example.myapplication.presentation.viewmodel.QuizViewModel
import com.example.myapplication.presentation.viewmodel.ResultViewModel
import com.example.myapplication.presentation.viewmodel.SettingsEvent
import com.example.myapplication.presentation.viewmodel.SettingsViewModel
import com.example.myapplication.presentation.viewmodel.StatsViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    onContinueAsGuest: () -> Unit
) {
    // Shared across auth/profile screens so state survives navigation between them.
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    fun goHome() = navController.navigate(Screen.Home.route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }

    fun goLogin() = navController.navigate(Screen.Login.route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }

    fun startQuiz(mode: QuizMode, category: QuizCategory? = null) =
        navController.navigate(Screen.Quiz.create(mode, category)) { launchSingleTop = true }

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
                onContinueAsGuest = {
                    onContinueAsGuest()
                    goHome()
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
                            snackbarHostState.showSnackbar(context.getString(R.string.reward_granted, event.amount))
                        is HomeEvent.Message -> snackbarHostState.showSnackbar(context.getString(event.res))
                    }
                }
            }

            // No interstitials here: Home -> secondary screens and Home -> Quiz never show one.
            // The only interstitial break is Result -> Home (between quizzes), see below.
            HomeScreen(
                uiState = homeState,
                snackbarHostState = snackbarHostState,
                onStartDaily = { startQuiz(QuizMode.DAILY) },
                onStartCategory = { startQuiz(QuizMode.CATEGORY, it) },
                onWatchRewardedAd = { activity?.let(homeViewModel::showRewardedAd) },
                onBookmarksClick = { navController.navigate(Screen.Bookmarks.route) },
                onStatsClick = { navController.navigate(Screen.Stats.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onAboutClick = { navController.navigate(Screen.About.route) }
            )
        }

        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument(QuizViewModel.ARG_MODE) { type = NavType.StringType },
                navArgument(QuizViewModel.ARG_CATEGORY) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val quizViewModel: QuizViewModel = hiltViewModel()
            val quizState by quizViewModel.uiState.collectAsStateWithLifecycle()
            val activity = rememberActivity()
            val quizSnackbar = remember { SnackbarHostState() }

            LaunchedEffect(quizViewModel) {
                quizViewModel.events.collect { event ->
                    when (event) {
                        is QuizEvent.Finished -> navController.navigate(Screen.Result.create(event.attemptId)) {
                            popUpTo(Screen.Home.route)
                            launchSingleTop = true
                        }
                        is QuizEvent.Message -> quizSnackbar.showSnackbar(context.getString(event.res))
                    }
                }
            }

            // Pause the 20 s timer while the app is in the background.
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner, quizViewModel) {
                val observer = LifecycleEventObserver { _, e ->
                    when (e) {
                        Lifecycle.Event.ON_PAUSE -> quizViewModel.setPaused(true)
                        Lifecycle.Event.ON_RESUME -> quizViewModel.setPaused(false)
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            QuizScreen(
                uiState = quizState,
                snackbarHostState = quizSnackbar,
                onSelect = quizViewModel::selectOption,
                onNext = quizViewModel::next,
                onHint = { activity?.let(quizViewModel::useHint) },
                onExtraTime = { activity?.let(quizViewModel::useExtraTime) },
                onToggleBookmark = quizViewModel::toggleBookmark,
                onExit = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(navArgument(ResultViewModel.ARG_ATTEMPT_ID) { type = NavType.LongType })
        ) {
            val resultViewModel: ResultViewModel = hiltViewModel()
            val resultState by resultViewModel.uiState.collectAsStateWithLifecycle()
            val activity = rememberActivity()

            fun backToHome() {
                val go = { navController.popBackStack(Screen.Home.route, inclusive = false); Unit }
                // Natural break between quizzes: AdsManager counts the action and shows an
                // interstitial only if pacing allows (>= 2 actions, >= 2 min apart, consent given).
                if (activity != null) resultViewModel.onNaturalBreak(activity, go) else go()
            }

            ResultScreen(
                uiState = resultState,
                onHome = ::backToHome,
                onPlayAgain = {
                    val review = resultState.review
                    val mode = review?.mode ?: QuizMode.CATEGORY
                    if (mode == QuizMode.CATEGORY && review?.category == null) {
                        backToHome()
                    } else {
                        navController.navigate(Screen.Quiz.create(mode, review?.category)) {
                            popUpTo(Screen.Home.route)
                            launchSingleTop = true
                        }
                    }
                },
                onToggleBookmark = resultViewModel::toggleBookmark
            )
        }

        composable(Screen.Bookmarks.route) {
            val backWithBreak = rememberBackWithBreak(navController)
            val vm: BookmarksViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            BookmarksScreen(
                uiState = state,
                onPractice = { startQuiz(QuizMode.BOOKMARKS) },
                onRemove = vm::removeBookmark,
                onBackClick = backWithBreak
            )
        }

        composable(Screen.Stats.route) {
            val backWithBreak = rememberBackWithBreak(navController)
            val vm: StatsViewModel = hiltViewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            StatsScreen(uiState = state, onBackClick = backWithBreak)
        }

        composable(Screen.Profile.route) {
            val backWithBreak = rememberBackWithBreak(navController)
            ProfileScreen(
                uiState = authState,
                snackbarHostState = snackbarHostState,
                onSaveProfile = { name, photo -> authViewModel.updateProfile(name, photo) },
                onVerifyEmail = authViewModel::sendEmailVerification,
                onLogout = authViewModel::logout,
                onDeleteAccount = { password -> authViewModel.deleteAccount(password) },
                onCancelReauth = authViewModel::cancelReauth,
                onSignInClick = { navController.navigate(Screen.Login.route) },
                onBackClick = backWithBreak
            )
        }

        composable(Screen.Settings.route) {
            val backWithBreak = rememberBackWithBreak(navController)
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val activity = rememberActivity()

            LaunchedEffect(settingsViewModel) {
                settingsViewModel.events.collect { event ->
                    when (event) {
                        SettingsEvent.DataCleared -> snackbarHostState.showSnackbar(context.getString(R.string.settings_reset_done))
                        is SettingsEvent.Message -> snackbarHostState.showSnackbar(context.getString(event.res))
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
                onLanguageChange = settingsViewModel::setLanguage,
                onPrivacyOptionsClick = { activity?.let(settingsViewModel::openPrivacyOptions) },
                onClearData = settingsViewModel::clearData,
                onDeleteAccountClick = { navController.navigate(Screen.Profile.route) },
                onBackClick = backWithBreak
            )
        }

        composable(Screen.About.route) {
            val backWithBreak = rememberBackWithBreak(navController)
            AboutScreen(onBackClick = backWithBreak)
        }
    }
}

/**
 * Back from a secondary screen to Home = natural break. Shows an interstitial only when the
 * pacer allows (>= 2 user actions and >= 2 minutes since the last one); otherwise just goes back.
 */
@Composable
private fun rememberBackWithBreak(navController: NavHostController): () -> Unit {
    val bannerViewModel: BannerViewModel = hiltViewModel()
    val activity = rememberActivity()
    val back: () -> Unit = remember(navController, activity) {
        {
            val go = { navController.popBackStack(); Unit }
            if (activity != null) bannerViewModel.onNaturalBreak(activity, go) else go()
        }
    }
    BackHandler(onBack = back)
    return back
}
