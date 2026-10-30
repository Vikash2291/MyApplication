package com.example.myapplication.presentation.ui.screens

import androidx.compose.ui.text.style.TextAlign
import com.example.myapplication.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.presentation.ui.components.AppCard
import com.example.myapplication.presentation.ui.components.AppTextField
import com.example.myapplication.presentation.ui.components.AppTopBar
import com.example.myapplication.presentation.ui.components.ErrorText
import com.example.myapplication.presentation.ui.components.PasswordTextField
import com.example.myapplication.presentation.ui.components.PrimaryButton
import com.example.myapplication.presentation.ui.components.SecondaryButton
import com.example.myapplication.presentation.ui.components.SectionHeader
import com.example.myapplication.presentation.viewmodel.AuthUiState
import java.text.DateFormat
import java.util.Date

@Composable
fun ProfileScreen(
    uiState: AuthUiState,
    snackbarHostState: SnackbarHostState,
    onSaveProfile: (displayName: String, photoUrl: String) -> Unit,
    onVerifyEmail: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: (password: String?) -> Unit,
    onCancelReauth: () -> Unit,
    onSignInClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val user = uiState.user
    var name by rememberSaveable { mutableStateOf(user?.displayName.orEmpty()) }
    var photoUrl by rememberSaveable { mutableStateOf(user?.photoUrl.orEmpty()) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(user?.id) {
        name = user?.displayName.orEmpty()
        photoUrl = user?.photoUrl.orEmpty()
    }

    Scaffold(
        topBar = { AppTopBar(stringResource(R.string.profile_title), onNavigateUp = onBackClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Avatar(user?.photoUrl, user?.displayName ?: user?.email)
            if (user == null) {
                // Guest: progress lives on this device only; offer sign-in instead of account actions.
                Text(stringResource(R.string.profile_guest_title), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(R.string.profile_guest_body),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                PrimaryButton(
                    text = stringResource(R.string.login_sign_in),
                    onClick = onSignInClick,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
            Text(user.displayName ?: stringResource(R.string.profile_no_name), style = MaterialTheme.typography.titleLarge)
            Text(user.email, color = MaterialTheme.colorScheme.onSurfaceVariant)

            run {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (user.isEmailVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (user.isEmailVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(if (user.isEmailVerified) R.string.profile_email_verified else R.string.profile_email_not_verified))
                    if (!user.isEmailVerified) {
                        TextButton(onClick = onVerifyEmail) { Text(stringResource(R.string.profile_send_link)) }
                    }
                }
            }

            SectionHeader(stringResource(R.string.profile_edit), Modifier.fillMaxWidth())
            AppCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(name, { name = it }, stringResource(R.string.profile_display_name))
                    AppTextField(photoUrl, { photoUrl = it }, stringResource(R.string.profile_photo_url))
                    ErrorText(uiState.error)
                    PrimaryButton(
                        text = stringResource(R.string.profile_save),
                        onClick = { onSaveProfile(name, photoUrl) },
                        loading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            run {
                SectionHeader(stringResource(R.string.profile_account), Modifier.fillMaxWidth())
                AppCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoRow(stringResource(R.string.profile_member_since), formatTimestamp(user.createdAt))
                        InfoRow(stringResource(R.string.profile_last_sign_in), formatTimestamp(user.lastLoginAt))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            SecondaryButton(
                text = stringResource(R.string.profile_sign_out),
                onClick = onLogout,
                icon = Icons.AutoMirrored.Filled.Logout,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.profile_delete_account), color = MaterialTheme.colorScheme.error)
            }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.profile_delete_title)) },
            text = {
                Text(stringResource(R.string.profile_delete_body))
            },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDeleteAccount(null) }) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) } }
        )
    }
    if (uiState.isReauthRequired) {
        ReauthDialog(
            isLoading = uiState.isLoading,
            error = uiState.error,
            onConfirm = { onDeleteAccount(it) },
            onDismiss = onCancelReauth
        )
    }
}

@Composable
private fun ReauthDialog(
    isLoading: Boolean,
    error: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reauth_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.reauth_body))
                PasswordTextField(password, { password = it }, stringResource(R.string.field_password), onImeAction = { onConfirm(password) })
                ErrorText(error)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(password) }, enabled = password.isNotBlank() && !isLoading) {
                Text(stringResource(R.string.profile_delete_account), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun Avatar(photoUrl: String?, fallbackName: String?) {
    val modifier = Modifier
        .size(96.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer)
    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(model = photoUrl, contentDescription = stringResource(R.string.cd_profile_photo), contentScale = ContentScale.Crop, modifier = modifier)
    } else {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                fallbackName?.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}

fun formatTimestamp(timestamp: Long): String =
    if (timestamp <= 0L) "—" else DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timestamp))
