package com.example.myapplication.presentation.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdUnits
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.ThemeMode
import com.example.myapplication.presentation.ui.components.AppCard
import com.example.myapplication.presentation.ui.components.AppTopBar
import com.example.myapplication.presentation.ui.components.SectionHeader
import com.example.myapplication.presentation.viewmodel.SettingsUiState

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    onThemeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onAdMobChange: (Boolean) -> Unit,
    onPrivacyOptionsClick: () -> Unit,
    onClearData: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val s = uiState.settings
    var confirmClear by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar("Settings", onNavigateUp = onBackClick) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader("Appearance")
            AppCard {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Text("Theme", style = MaterialTheme.typography.titleMedium)
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = s.themeMode == mode,
                                onClick = { onThemeChange(mode) },
                                label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        SwitchRow(Icons.Default.ColorLens, "Dynamic colors", "Match your wallpaper", s.dynamicColor, onDynamicColorChange)
                    }
                }
            }

            SectionHeader("Notifications")
            AppCard {
                Column(Modifier.padding(16.dp)) {
                    SwitchRow(Icons.Default.Notifications, "Push notifications", "News and updates", s.notificationsEnabled, onNotificationsChange)
                }
            }

            SectionHeader("Ads & privacy")
            AppCard {
                Column(Modifier.padding(16.dp)) {
                    SwitchRow(Icons.Default.AdUnits, "Ads", "Banner, interstitial and app open ads (via AdMob)", s.adMobEnabled, onAdMobChange)
                    // Required by UMP when the user is in a regulated region (e.g. EEA/UK, US states).
                    if (uiState.isPrivacyOptionsRequired) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        TextButton(onClick = onPrivacyOptionsClick) {
                            Icon(Icons.Default.PrivacyTip, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Privacy options")
                        }
                    }
                }
            }

            SectionHeader("Data")
            AppCard {
                Column(Modifier.padding(8.dp)) {
                    TextButton(onClick = { confirmClear = true }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text("Reset all settings", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = onDeleteAccountClick) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete account…", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Reset settings?") },
            text = { Text("All preferences and reward points on this device will be reset.") },
            confirmButton = { TextButton(onClick = { confirmClear = false; onClearData() }) { Text("Reset") } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
