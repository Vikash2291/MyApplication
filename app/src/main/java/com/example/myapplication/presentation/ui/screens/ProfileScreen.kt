package com.example.myapplication.presentation.ui.screens

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
    onDeleteAccount: () -> Unit,
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
        topBar = { AppTopBar("Profile", onNavigateUp = onBackClick) },
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
            Text(user?.displayName ?: "No name set", style = MaterialTheme.typography.titleLarge)
            Text(user?.email.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (user != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (user.isEmailVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (user.isEmailVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(if (user.isEmailVerified) "Email verified" else "Email not verified")
                    if (!user.isEmailVerified) {
                        TextButton(onClick = onVerifyEmail) { Text("Send link") }
                    }
                }
            }

            SectionHeader("Edit profile", Modifier.fillMaxWidth())
            AppCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(name, { name = it }, "Display name")
                    AppTextField(photoUrl, { photoUrl = it }, "Photo URL (optional)")
                    ErrorText(uiState.error)
                    PrimaryButton(
                        text = "Save changes",
                        onClick = { onSaveProfile(name, photoUrl) },
                        loading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (user != null) {
                SectionHeader("Account", Modifier.fillMaxWidth())
                AppCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoRow("Member since", formatTimestamp(user.createdAt))
                        InfoRow("Last sign-in", formatTimestamp(user.lastLoginAt))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            SecondaryButton(
                text = "Sign out",
                onClick = onLogout,
                icon = Icons.AutoMirrored.Filled.Logout,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.size(6.dp))
                Text("Delete account", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete account?") },
            text = { Text("This permanently deletes your account and data. You may need to sign in again first.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDeleteAccount() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun Avatar(photoUrl: String?, fallbackName: String?) {
    val modifier = Modifier
        .size(96.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer)
    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(model = photoUrl, contentDescription = "Profile photo", contentScale = ContentScale.Crop, modifier = modifier)
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
