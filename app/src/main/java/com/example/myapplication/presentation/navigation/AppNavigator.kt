package com.example.myapplication.presentation.navigation

import com.example.myapplication.domain.quiz.QuizCategory
import com.example.myapplication.domain.quiz.QuizMode

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")
    data object Home : Screen("home")
    data object Settings : Screen("settings")
    data object Profile : Screen("profile")
    data object About : Screen("about")
    data object Bookmarks : Screen("bookmarks")
    data object Stats : Screen("stats")

    data object Quiz : Screen("quiz/{mode}?category={category}") {
        fun create(mode: QuizMode, category: QuizCategory? = null): String =
            "quiz/${mode.name}" + (category?.let { "?category=${it.name}" } ?: "")
    }

    data object Result : Screen("result/{attemptId}") {
        fun create(attemptId: Long): String = "result/$attemptId"
    }
}
