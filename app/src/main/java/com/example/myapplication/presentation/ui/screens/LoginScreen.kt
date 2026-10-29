package com.example.myapplication.presentation.ui.screens

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.presentation.ui.components.AppTextField
import com.example.myapplication.presentation.ui.components.ErrorText
import com.example.myapplication.presentation.ui.components.PasswordTextField
import com.example.myapplication.presentation.ui.components.PrimaryButton
import com.example.myapplication.presentation.viewmodel.AuthUiState

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onLoginClick: (email: String, password: String) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onContinueAsGuest: () -> Unit = {},
    onInputChanged: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val submit = { onLoginClick(email, password) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = null,
            modifier = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))

        AppTextField(
            value = email,
            onValueChange = { email = it; onInputChanged() },
            label = stringResource(R.string.field_email),
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.testTag("email")
        )
        Spacer(Modifier.height(12.dp))
        PasswordTextField(
            value = password,
            onValueChange = { password = it; onInputChanged() },
            label = stringResource(R.string.field_password),
            leadingIcon = Icons.Default.Lock,
            imeAction = ImeAction.Done,
            onImeAction = submit,
            modifier = Modifier.testTag("password")
        )

        TextButton(onClick = onForgotPasswordClick, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.login_forgot))
        }

        ErrorText(uiState.error, Modifier.padding(vertical = 8.dp))

        PrimaryButton(
            text = stringResource(R.string.login_sign_in),
            onClick = submit,
            loading = uiState.isLoading,
            modifier = Modifier.fillMaxWidth().testTag("login_button")
        )
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.login_no_account), color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onRegisterClick) { Text(stringResource(R.string.login_sign_up)) }
        }
        TextButton(onClick = onContinueAsGuest, modifier = Modifier.testTag("guest_button")) {
            Text(stringResource(R.string.login_guest))
        }
    }
}
