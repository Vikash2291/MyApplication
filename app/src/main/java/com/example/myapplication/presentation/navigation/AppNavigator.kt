package com.example.myapplication.presentation.navigation

import androidx.navigation.NavHostController

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
}

class AppNavigator(private val navController: NavHostController) {

    fun navigateTo(screen: Screen) {
        navController.navigate(screen.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateTo(screen: Screen, args: Bundle) {
        navController.navigate("${screen.route}?${args.toQueryString()}") {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateUp() {
        navController.navigateUp()
    }

    fun clearBackStack() {
        navController.popBackStack(navController.graph.findStartDestination().id, false)
    }
}

fun Bundle.toQueryString(): String {
    return keySet().map { "$it=${getString(it)}" }.joinToString("&")
}