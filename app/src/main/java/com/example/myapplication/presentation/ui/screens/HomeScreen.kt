package com.example.myapplication.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.domain.quiz.QuizCategory
import com.example.myapplication.presentation.ui.components.AdaptiveBannerSlot
import com.example.myapplication.presentation.ui.components.AppCard
import com.example.myapplication.presentation.ui.components.AppTopBar
import com.example.myapplication.presentation.ui.components.PrimaryButton
import com.example.myapplication.presentation.ui.components.SectionHeader
import com.example.myapplication.presentation.ui.components.icon
import com.example.myapplication.presentation.ui.components.labelRes
import com.example.myapplication.presentation.viewmodel.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onStartDaily: () -> Unit,
    onStartCategory: (QuizCategory) -> Unit,
    onWatchRewardedAd: () -> Unit,
    onBookmarksClick: () -> Unit,
    onStatsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.home_title),
                actions = {
                    IconButton(onClick = onBookmarksClick) {
                        Icon(Icons.Default.Bookmarks, contentDescription = stringResource(R.string.bookmarks_title))
                    }
                    IconButton(onClick = onStatsClick) {
                        Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.stats_title))
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = stringResource(R.string.profile_title))
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Banner lives in a fixed-height slot at the very bottom, away from the play buttons.
            if (uiState.showBanner && uiState.bannerAdUnitId.isNotBlank()) {
                AdaptiveBannerSlot(uiState.bannerAdUnitId)
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { GreetingCard(uiState) }
            item(span = { GridItemSpan(maxLineSpan) }) { DailyQuizCard(uiState.dailyCompletedToday, onStartDaily) }
            item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader(stringResource(R.string.home_categories)) }
            items(QuizCategory.entries, key = { it.name }) { category ->
                CategoryCard(category, uiState.questionCounts[category] ?: 0) { onStartCategory(category) }
            }
            item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader(stringResource(R.string.home_bonus_title)) }
            item(span = { GridItemSpan(maxLineSpan) }) { BonusCard(uiState.isRewardedAdReady, onWatchRewardedAd) }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = onAboutClick) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.about_title))
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingCard(uiState: HomeUiState) {
    AppCard {
        Column(Modifier.padding(20.dp)) {
            val name = uiState.user?.displayName?.takeIf { it.isNotBlank() }
            Text(
                if (name != null) stringResource(R.string.home_greeting_name, name) else stringResource(R.string.home_greeting),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(R.string.points_value, uiState.rewardPoints)) },
                    leadingIcon = { Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(pluralStringResource(R.plurals.streak_days, uiState.streak, uiState.streak)) },
                    leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) }
                )
            }
        }
    }
}

@Composable
private fun DailyQuizCard(completedToday: Boolean, onStart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (completedToday) Icons.Default.CheckCircle else Icons.Default.Today,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.daily_quiz_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        stringResource(if (completedToday) R.string.daily_quiz_done else R.string.daily_quiz_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = stringResource(if (completedToday) R.string.daily_quiz_replay else R.string.daily_quiz_play),
                onClick = onStart,
                icon = Icons.Default.PlayCircle,
                modifier = Modifier.fillMaxWidth().testTag("daily_quiz_play")
            )
        }
    }
}

@Composable
private fun CategoryCard(category: QuizCategory, questionCount: Int, onClick: () -> Unit) {
    AppCard(onClick = onClick) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(category.icon(), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(category.labelRes()),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                pluralStringResource(R.plurals.question_count, questionCount, questionCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BonusCard(isReady: Boolean, onWatch: () -> Unit) {
    AppCard {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.home_bonus_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = stringResource(if (isReady) R.string.home_bonus_watch else R.string.video_loading),
                onClick = onWatch,
                enabled = isReady,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
