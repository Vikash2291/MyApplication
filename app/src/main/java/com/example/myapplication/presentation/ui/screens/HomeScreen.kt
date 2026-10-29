package com.example.myapplication.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.myapplication.presentation.ui.components.AdaptiveBannerSlot
import com.example.myapplication.presentation.ui.components.AppCard
import com.example.myapplication.presentation.ui.components.AppTopBar
import com.example.myapplication.presentation.ui.components.PrimaryButton
import com.example.myapplication.presentation.ui.components.SectionHeader
import com.example.myapplication.presentation.viewmodel.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onWatchRewardedAd: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Home",
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Fixed-height slot at the very bottom, separated from the buttons above it.
            if (uiState.showBanner && uiState.bannerAdUnitId.isNotBlank()) {
                AdaptiveBannerSlot(uiState.bannerAdUnitId)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppCard {
                Column(Modifier.padding(20.dp)) {
                    val name = uiState.user?.displayName?.takeIf { it.isNotBlank() }
                        ?: uiState.user?.email
                        ?: "there"
                    Text("Welcome, $name!", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.size(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${uiState.rewardPoints} reward points",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            SectionHeader("Earn points")
            AppCard {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Bonus points (optional)", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Optional: watch a short video ad to earn bonus points.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.size(16.dp))
                    PrimaryButton(
                        text = if (uiState.isRewardedAdReady) "Watch video for points" else "Loading video…",
                        onClick = onWatchRewardedAd,
                        enabled = uiState.isRewardedAdReady,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            SectionHeader("More")
            NavigationCard(Icons.Default.AccountCircle, "Profile", "Your account details", onProfileClick)
            NavigationCard(Icons.Default.Settings, "Settings", "Theme, notifications, privacy", onSettingsClick)
            NavigationCard(Icons.Default.Info, "About", "Version and licenses", onAboutClick)
        }
    }
}

@Composable
private fun NavigationCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    AppCard(onClick = onClick) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}
