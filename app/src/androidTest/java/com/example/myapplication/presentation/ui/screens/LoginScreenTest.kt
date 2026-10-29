package com.example.myapplication.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.presentation.ui.theme.AppTheme
import com.example.myapplication.presentation.viewmodel.AuthUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun entersCredentialsAndSubmits() {
        var submitted: Pair<String, String>? = null
        composeRule.setContent {
            AppTheme {
                LoginScreen(
                    uiState = AuthUiState(),
                    onLoginClick = { e, p -> submitted = e to p },
                    onRegisterClick = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("email").performTextInput("user@example.com")
        composeRule.onNodeWithTag("password").performTextInput("secret1")
        composeRule.onNodeWithTag("login_button").performClick()

        assertEquals("user@example.com" to "secret1", submitted)
    }

    @Test
    fun showsErrorMessage() {
        composeRule.setContent {
            AppTheme {
                LoginScreen(
                    uiState = AuthUiState(error = "Invalid credentials"),
                    onLoginClick = { _, _ -> },
                    onRegisterClick = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        composeRule.onNodeWithText("Invalid credentials").assertIsDisplayed()
    }
}
