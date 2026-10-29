package com.example.myapplication.presentation.ui.screens

import com.example.myapplication.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.presentation.ui.components.AppTextField
import com.example.myapplication.presentation.ui.components.ErrorText
import com.example.myapplication.presentation.ui.components.PasswordTextField
import com.example.myapplication.presentation.ui.components.PrimaryButton
import com.example.myapplication.presentation.viewmodel.AuthUiState

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onRegisterClick: (displayName: String, email: String, password: String, confirm: String) -> Unit,
    onLoginClick: () -> Unit,
    onInputChanged: () -> Unit = {}
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    val submit = { onRegisterClick(name, email, password, confirm) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.register_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.register_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))

        AppTextField(name, { name = it; onInputChanged() }, stringResource(R.string.field_full_name), leadingIcon = Icons.Default.Person)
        Spacer(Modifier.height(12.dp))
        AppTextField(
            email, { email = it; onInputChanged() }, stringResource(R.string.field_email),
            leadingIcon = Icons.Default.Email, keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(12.dp))
        PasswordTextField(
            password, { password = it; onInputChanged() }, stringResource(R.string.field_password_min),
            leadingIcon = Icons.Default.Lock, imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(12.dp))
        PasswordTextField(
            confirm, { confirm = it; onInputChanged() }, stringResource(R.string.field_confirm_password),
            leadingIcon = Icons.Default.Lock, onImeAction = submit,
            isError = confirm.isNotEmpty() && confirm != password
        )
        Spacer(Modifier.height(16.dp))

        ErrorText(uiState.error, Modifier.padding(bottom = 8.dp))

        PrimaryButton(
            text = stringResource(R.string.register_title),
            onClick = submit,
            loading = uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.register_have_account), color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onLoginClick) { Text(stringResource(R.string.login_sign_in)) }
        }
    }
}
