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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.ui.components.AppCard
import com.example.myapplication.presentation.ui.components.PrimaryButton
import com.example.myapplication.presentation.ui.components.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AdUnits
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Cloud

@Composable
fun SettingsScreen(
    onThemeChange: (Int) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onAdConsentChange: (Boolean) -> Unit,
    onGdprConsentChange: (Boolean) -> Unit,
    onAdMobChange: (Boolean) -> Unit,
    onUnityAdsChange: (Boolean) -> Unit,
    onIronSourceChange: (Boolean) -> Unit,
    onClearData: () -> Unit,
    onBackClick: () -> Unit,
    uiState: kotlinx.coroutines.flow.StateFlow<com.example.myapplication.presentation.viewmodel.SettingsUiState>
) {
    val state by uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = "Settings",
                navigateUp = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Appearance Section
                SectionHeader(title = "Appearance")
                AppCard {
                    SettingRow(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        subtitle = "Choose app appearance"
                    ) {
                        ThemeSelector(
                            currentMode = state.themeMode,
                            onThemeChange = onThemeChange
                        )
                    }
                }

                // Notifications Section
                SectionHeader(title = "Notifications")
                AppCard {
                    SettingRow(
                        icon = Icons.Default.Notifications,
                        title = "Push Notifications",
                        subtitle = "Receive updates and offers"
                    ) {
                        Switch(
                            checked = state.notificationsEnabled,
                            onCheckedChange = onNotificationsChange,
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                trackColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }

                // Ad Networks Section
                SectionHeader(title = "Ad Networks")
                AppCard {
                    SettingRow(
                        icon = Icons.Default.AdUnits,
                        title = "AdMob",
                        subtitle = "Google AdMob mediation"
                    ) {
                        Switch(
                            checked = state.adMobEnabled,
                            onCheckedChange = onAdMobChange,
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                trackColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    androidx.compose.foundation.layout.Divider(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 56.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    SettingRow(
                        icon = Icons.Default.Cloud,
                        title = "Unity Ads",
                        subtitle = "Unity LevelPlay mediation"
                    ) {
                        Switch(
                            checked = state.unityAdsEnabled,
                            onCheckedChange = onUnityAdsChange,
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                trackColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    androidx.compose.foundation.layout.Divider(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 56.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    SettingRow(
                        icon = Icons.Default.ToggleOn,
                        title = "ironSource",
                        subtitle = "ironSource mediation (pending approval)"
                    ) {
                        Switch(
                            checked = state.ironSourceEnabled,
                            onCheckedChange = onIronSourceChange,
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                trackColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                }

                // Privacy Section
                SectionHeader(title = "Privacy & Ads")
                AppCard {
                    SettingRow(
                        icon = Icons.Default.PrivacyTip,
                        title = "Ad Personalization",
                        subtitle = "Allow personalized ads based on your usage"
                    ) {
                        Switch(
                            checked = state.adPersonalizationConsent,
                            onCheckedChange = onAdConsentChange,
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                trackColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    androidx.compose.foundation.layout.Divider(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 56.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    SettingRow(
                        icon = Icons.Default.Security,
                        title = "GDPR Consent",
                        subtitle = "Data processing consent for EU users"
                    ) {
                        Switch(
                            checked = state.gdprConsent,
                            onCheckedChange = onGdprConsentChange,
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                trackColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                }

                // Data Section
                SectionHeader(title = "Data Management")
                AppCard {
                    SettingRow(
                        icon = Icons.Default.DeleteForever,
                        title = "Clear All Data",
                        subtitle = "Reset app to initial state",
                        iconColor = androidx.compose.material3.MaterialTheme.colorScheme.error
                    ) {
                        PrimaryButton(
                            text = "Clear Data",
                            onClick = onClearData,
                            modifier = Modifier.width(140.dp),
                            enabled = true
                        )
                    }
                }

                // About Section
                SectionHeader(title = "About")
                AppCard {
                    SettingRow(
                        icon = Icons.Default.Info,
                        title = "App Version",
                        subtitle = "1.0.0 (Build 1)"
                    ) {
                        Text(
                            text = "1.0.0",
                            fontSize = 16.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    androidx.compose.foundation.layout.Divider(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 56.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    SettingRow(
                        icon = Icons.Default.Info,
                        title = "Licenses",
                        subtitle = "Open source licenses"
                    ) {
                        // Navigate to licenses
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            androidx.compose.foundation.layout.Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        content()
    }
}

@Composable
fun ThemeSelector(
    currentMode: Int,
    onThemeChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThemeOption(
            label = "System",
            icon = Icons.Default.BrightnessAuto,
            isSelected = currentMode == 0,
            onClick = { onThemeChange(0) }
        )
        ThemeOption(
            label = "Light",
            icon = Icons.Default.LightMode,
            isSelected = currentMode == 1,
            onClick = { onThemeChange(1) }
        )
        ThemeOption(
            label = "Dark",
            icon = Icons.Default.DarkMode,
            isSelected = currentMode == 2,
            onClick = { onThemeChange(2) }
        )
    }
}

@Composable
fun ThemeOption(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = if (isSelected) {
        androidx.compose.material3.MaterialTheme.colorScheme.primary to androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
    } else {
        androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest to androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    }

    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .height(48.dp),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = colors.first,
            contentColor = colors.second,
            borderColor = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
        ),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        border = androidx.compose.ui.graphics.drawscope.Stroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontals,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.second,
                modifier = Modifier.size(20.dp)
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.second
            )
        }
    }
}