package com.example.myapplication.core.common

/** Pure-Kotlin input validation shared by the UI and domain layers. */
object Validators {

    private val EMAIL_REGEX =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_DISPLAY_NAME_LENGTH = 50

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    fun isValidDisplayName(name: String): Boolean =
        name.isNotBlank() && name.trim().length <= MAX_DISPLAY_NAME_LENGTH

    /** Returns an error message, or null when the credentials are valid. */
    fun validateLogin(email: String, password: String): String? = when {
        email.isBlank() -> "Email is required"
        !isValidEmail(email) -> "Enter a valid email address"
        password.isBlank() -> "Password is required"
        else -> null
    }

    /** Returns an error message, or null when the registration form is valid. */
    fun validateRegistration(
        displayName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? = when {
        !isValidDisplayName(displayName) -> "Enter your name (max $MAX_DISPLAY_NAME_LENGTH characters)"
        !isValidEmail(email) -> "Enter a valid email address"
        !isValidPassword(password) -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
        password != confirmPassword -> "Passwords do not match"
        else -> null
    }
}
