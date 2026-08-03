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
import com.example.myapplication.presentation.ui.components.CustomTextField
import com.example.myapplication.presentation.ui.components.PrimaryButton
import com.example.myapplication.presentation.ui.components.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun ProfileScreen(
    onUpdateProfile: (String, String?) -> Unit,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
    uiState: kotlinx.coroutines.flow.StateFlow<com.example.myapplication.presentation.viewmodel.AuthUiState>
) {
    val state by uiState.collectAsState()
    val user = state.user

    var displayName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(user?.displayName ?: "") }
    var photoUrl by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(user?.photoUrl) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = "Profile",
                navigateUp = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Header
                AppCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontals,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Profile Image
                        Box(
                            modifier = Modifier.size(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            photoUrl?.let { url ->
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.foundation.rememberAsyncImagePainter(url),
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(100.dp).clip(androidx.compose.foundation.shape.CircleShape)
                                )
                            } ?: androidx.compose.foundation.Image(
                                painter = painterResource(id = com.example.myapplication.R.drawable.ic_launcher),
                                contentDescription = "Profile",
                                contentScale = ContentScale.FitCenter,
                                modifier = Modifier.size(100.dp).clip(androidx.compose.foundation.shape.CircleShape)
                            )
                            
                            // Camera Icon Overlay
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .offset(x = -4.dp, y = -4.dp)
                                        .background(
                                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        )
                                        .align(Alignment.BottomEnd)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Change photo",
                                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp).align(Alignment.Center)
                                    )
                                }
                            }
                        }

                        // Name & Email
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontals,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = displayName.ifBlank { "User" },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                            )
                            user?.let { u ->
                                Text(
                                    text = u.email,
                                    fontSize = 16.sp,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Email Verified Badge
                        user?.let { u ->
                            if (u.isEmailVerified) {
                                Row(
                                    modifier = Modifier.padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Email verified",
                                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    androidx.compose.foundation.layout.Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Email verified",
                                        fontSize = 12.sp,
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                Text(
                                    text = "Email not verified",
                                    fontSize = 12.sp,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Edit Profile Section
                AppCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Edit Profile",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )

                        CustomTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = "Display Name",
                            placeholder = "Enter your name",
                            leadingIcon = Icons.Default.Person,
                            isError = displayName.isNotBlank() && displayName.length < 2
                        )

                        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))

                        PrimaryButton(
                            text = "Save Changes",
                            onClick = { onUpdateProfile(displayName, photoUrl) },
                            icon = Icons.Default.Check
                        )
                    }
                }

                // Account Info Section
                AppCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Account Info",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )

                        InfoRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = user?.email ?: "Not set"
                        )

                        androidx.compose.foundation.layout.Divider(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(start = 56.dp)
                        )

                        InfoRow(
                            icon = Icons.Default.Person,
                            label = "User ID",
                            value = user?.id.take(8) ?: "Unknown"
                        )

                        androidx.compose.foundation.layout.Divider(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(start = 56.dp)
                        )

                        InfoRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Member since",
                            value = user?.createdAt?.let { formatTimestamp(it) } ?: "Unknown"
                        )
                    }
                }

                // Danger Zone
                AppCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Danger Zone",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )

                        PrimaryButton(
                            text = "Sign Out",
                            onClick = onLogoutClick,
                            icon = Icons.Default.Logout,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            androidx.compose.foundation.layout.Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = androidx.compose.ui.text.TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    return java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
}