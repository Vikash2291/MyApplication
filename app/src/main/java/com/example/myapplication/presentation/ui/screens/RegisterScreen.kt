package com.example.myapplication.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.ui.components.CustomTextField
import com.example.myapplication.presentation.ui.components.PrimaryButton
import com.example.myapplication.presentation.ui.components.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    onAuthEvent: (com.example.myapplication.presentation.viewmodel.AuthEvent) -> Unit,
    uiState: kotlinx.coroutines.flow.StateFlow<com.example.myapplication.presentation.viewmodel.AuthUiState>
) {
    var email by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var password by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var confirmPassword by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var displayName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var showPassword by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showConfirmPassword by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val state by uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.myapplication.R.drawable.ic_launcher),
                    contentDescription = "App Logo",
                    contentScale = androidx.compose.ui.layout.ContentScale.FitCenter
                )
            }

            androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))

            // Title
            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))

            Text(
                text = "Get started with your free account",
                fontSize = 16.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            androidx.compose.foundation.layout.Spacer(Modifier.height(32.dp))

            // Display Name Field
            CustomTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = "Display Name",
                placeholder = "Enter your name",
                leadingIcon = Icons.Default.Person,
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                isError = displayName.isNotBlank() && displayName.length < 2
            )

            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))

            // Email Field
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "Enter your email",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                isError = email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            )

            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))

            // Password Field
            CustomTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Enter password (min 6 characters)",
                leadingIcon = Icons.Default.Lock,
                trailingIcon = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                trailingIconClick = { showPassword = !showPassword },
                visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                isError = password.isNotBlank() && password.length < 6
            )

            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))

            // Confirm Password Field
            CustomTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                placeholder = "Confirm your password",
                leadingIcon = Icons.Default.Lock,
                trailingIcon = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                trailingIconClick = { showConfirmPassword = !showConfirmPassword },
                visualTransformation = if (showConfirmPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = androidx.compose.ui.text.input.KeyboardActions(
                    onDone = { 
                        if (validateForm()) onRegisterClick(email, password, displayName) 
                    }
                ),
                isError = confirmPassword.isNotBlank() && confirmPassword != password,
                errorMessage = if (confirmPassword.isNotBlank() && confirmPassword != password) "Passwords don't match" else null
            )

            androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))

            // Error Message
            state.error?.let { error ->
                Text(
                    text = error,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }

            // Register Button
            PrimaryButton(
                text = "Create Account",
                onClick = { if (validateForm()) onRegisterClick(email, password, displayName) },
                loading = state.isLoading
            )

            androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))

            // Login Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                TextButton(
                    text = "Sign In",
                    onClick = onLoginClick
                )
            }
        }
    }

    fun validateForm(): Boolean {
        return displayName.length >= 2 &&
               android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
               password.length >= 6 &&
               password == confirmPassword
    }
}