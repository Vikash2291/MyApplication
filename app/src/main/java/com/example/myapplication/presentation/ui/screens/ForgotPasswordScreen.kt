package com.example.myapplication.presentation.ui.screens

import com.example.myapplication.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.presentation.ui.components.AppTextField
import com.example.myapplication.presentation.ui.components.AppTopBar
import com.example.myapplication.presentation.ui.components.ErrorText
import com.example.myapplication.presentation.ui.components.PrimaryButton
import com.example.myapplication.presentation.viewmodel.AuthUiState

@Composable
fun ForgotPasswordScreen(
    uiState: AuthUiState,
    onResetClick: (email: String) -> Unit,
    onBackClick: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }

    Scaffold(topBar = { AppTopBar(title = stringResource(R.string.forgot_title), onNavigateUp = onBackClick) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                stringResource(R.string.forgot_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.field_email),
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                onImeAction = { onResetClick(email) }
            )
            Spacer(Modifier.height(16.dp))
            ErrorText(uiState.error, Modifier.padding(bottom = 8.dp))
            PrimaryButton(
                text = stringResource(R.string.forgot_send),
                onClick = { onResetClick(email) },
                loading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
