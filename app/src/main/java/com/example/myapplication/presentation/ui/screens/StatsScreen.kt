package com.example.myapplication.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.domain.quiz.CategoryProgress
import com.example.myapplication.presentation.ui.components.AppCard
import com.example.myapplication.presentation.ui.components.AppTopBar
import com.example.myapplication.presentation.ui.components.SectionHeader
import com.example.myapplication.presentation.ui.components.StatTile
import com.example.myapplication.presentation.ui.components.icon
import com.example.myapplication.presentation.ui.components.labelRes
import com.example.myapplication.presentation.viewmodel.StatsUiState

@Composable
fun StatsScreen(uiState: StatsUiState, onBackClick: () -> Unit) {
    val stats = uiState.stats
    Scaffold(topBar = { AppTopBar(stringResource(R.string.stats_title), onNavigateUp = onBackClick) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(stats.quizzesPlayed.toString(), stringResource(R.string.stats_quizzes), Icons.Default.Quiz, Modifier.weight(1f))
                    StatTile(stringResource(R.string.result_percent, stats.accuracyPercent), stringResource(R.string.stats_accuracy), Icons.Default.TaskAlt, Modifier.weight(1f))
                    StatTile(uiState.points.toString(), stringResource(R.string.stats_points), Icons.Default.Stars, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(stats.correctAnswers.toString(), stringResource(R.string.stats_correct), Icons.Default.CheckCircle, Modifier.weight(1f))
                    StatTile(uiState.streak.toString(), stringResource(R.string.stats_streak), Icons.Default.LocalFireDepartment, Modifier.weight(1f))
                    StatTile(uiState.bestStreak.toString(), stringResource(R.string.stats_best_streak), Icons.Default.EmojiEvents, Modifier.weight(1f))
                }
            }
            item { SectionHeader(stringResource(R.string.stats_by_category)) }
            items(stats.categories, key = { it.category.name }) { CategoryProgressCard(it) }
        }
    }
}

@Composable
private fun CategoryProgressCard(p: CategoryProgress) {
    AppCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(p.category.icon(), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(p.category.labelRes()), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    stringResource(R.string.stats_seen, p.seenQuestions, p.totalQuestions),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LinearProgressIndicator(progress = { p.coverage }, modifier = Modifier.fillMaxWidth())
            Text(
                stringResource(R.string.stats_category_line, p.accuracyPercent, p.bestScore, p.quizzesPlayed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
