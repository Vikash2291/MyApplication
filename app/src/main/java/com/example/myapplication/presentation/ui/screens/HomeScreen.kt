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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Info
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.FrameLayout

@Composable
fun HomeScreen(
    onRewardedAdClick: () -> Unit,
    onInterstitialAdClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onHomeEvent: (com.example.myapplication.presentation.viewmodel.HomeEvent) -> Unit,
    uiState: kotlinx.coroutines.flow.StateFlow<com.example.myapplication.presentation.viewmodel.HomeUiState>
) {
    val state by uiState.collectAsState()
    val authViewModel: com.example.myapplication.presentation.viewmodel.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
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

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Welcome Card
                AppCard {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        authViewModel.uiState.collectAsState().value?.user?.let { user ->
                            Text(
                                text = "Welcome, ${user.displayName ?: user.email}!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Your personalized dashboard",
                            fontSize = 16.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Ad Actions Section
                Text(
                    text = "Monetization",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                )

                // Rewarded Ad Card
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "Rewarded Video",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                                )
                            }
                            androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Watch a video to earn rewards",
                                fontSize = 14.sp,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        PrimaryButton(
                            text = state.isRewardedAdReady?.let { if (it) "Ready" else "Loading..." } ?: "Load",
                            onClick = onRewardedAdClick,
                            modifier = Modifier.width(120.dp),
                            enabled = state.isRewardedAdReady == true
                        )
                    }
                }

                // Interstitial Ad Card
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "Interstitial Ad",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                                )
                            }
                            androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Full screen ad between content",
                                fontSize = 14.sp,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        PrimaryButton(
                            text = state.isInterstitialAdReady?.let { if (it) "Ready" else "Loading..." } ?: "Load",
                            onClick = onInterstitialAdClick,
                            modifier = Modifier.width(120.dp),
                            enabled = state.isInterstitialAdReady == true
                        )
                    }
                }

                // Features Section
                Text(
                    text = "Features",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp).padding(top = 16.dp)
                )

                // Feature Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                                        icon = Icons.Default.AttachMoney,
                                        title = "In-App Purchases",
                                        description = "Unlock premium features",
                                        onClick = { /* TODO: Navigate to IAP screen */ }
                                    )
                                    FeatureCard(
                                        icon = Icons.Default.Info,
                                        title = "About App",
                                        description = "Version info & licenses",
                                        onClick = { /* TODO: Show about dialog */ }
                                    )
                }
            }
            
            // Banner Ad at bottom
            Box(modifier = Modifier.fillMaxWidth()) {
                AndroidView(
                    factory = { ctx ->
                        AdView(ctx).apply {
                            adUnitId = ctx.getString(R.string.admob_banner_ad_unit_id)
                            adSize = AdSize.BANNER
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            )
                            loadAd(AdRequest.Builder().build())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.15f),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
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
                    text = description,
                    fontSize = 14.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}