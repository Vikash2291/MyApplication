package com.example.myapplication.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.domain.quiz.AnswerRecord
import com.example.myapplication.domain.quiz.AppLanguage
import com.example.myapplication.domain.quiz.QuizMode
import com.example.myapplication.domain.quiz.ReviewItem
import com.example.myapplication.presentation.ui.components.AdaptiveBannerSlot
import com.example.myapplication.presentation.ui.components.AppCard
import com.example.myapplication.presentation.ui.components.LoadingScreen
import com.example.myapplication.presentation.ui.components.PrimaryButton
import com.example.myapplication.presentation.ui.components.QuizColors
import com.example.myapplication.presentation.ui.components.SecondaryButton
import com.example.myapplication.presentation.ui.components.SectionHeader
import com.example.myapplication.presentation.viewmodel.ResultUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    uiState: ResultUiState,
    onHome: () -> Unit,
    onPlayAgain: () -> Unit,
    onToggleBookmark: (String) -> Unit
) {
    BackHandler(onBack = onHome)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.result_title)) },
                navigationIcon = {
                    IconButton(onClick = onHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.result_home))
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.showBanner && uiState.bannerAdUnitId.isNotBlank()) {
                AdaptiveBannerSlot(uiState.bannerAdUnitId)
            }
        }
    ) { padding ->
        val review = uiState.review
        when {
            uiState.isLoading -> Box(Modifier.padding(padding)) { LoadingScreen() }
            review == null -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.error_generic))
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(stringResource(R.string.result_home), onHome)
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ScoreCard(
                        correct = review.correct,
                        total = review.total,
                        points = review.points,
                        streak = uiState.streak.takeIf { review.mode == QuizMode.DAILY }
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SecondaryButton(
                            text = stringResource(R.string.result_play_again),
                            onClick = onPlayAgain,
                            icon = Icons.Default.Replay,
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryButton(
                            text = stringResource(R.string.result_home),
                            onClick = onHome,
                            icon = Icons.Default.Home,
                            modifier = Modifier.weight(1f).testTag("result_home")
                        )
                    }
                }
                item { SectionHeader(stringResource(R.string.result_review)) }
                itemsIndexed(review.items, key = { index, item -> "$index-${item.question.id}" }) { index, item ->
                    ReviewCard(
                        number = index + 1,
                        item = item,
                        language = uiState.language,
                        bookmarked = item.question.id in uiState.bookmarkIds,
                        onToggleBookmark = { onToggleBookmark(item.question.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(correct: Int, total: Int, points: Int, streak: Int?) {
    val percent = if (total == 0) 0 else correct * 100 / total
    val message = when {
        percent == 100 -> R.string.result_msg_perfect
        percent >= 70 -> R.string.result_msg_great
        percent >= 40 -> R.string.result_msg_good
        else -> R.string.result_msg_keep_going
    }
    AppCard {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Text(
                stringResource(R.string.result_score, correct, total),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(stringResource(R.string.result_percent, percent), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(message), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.result_points_earned, points), style = MaterialTheme.typography.titleMedium)
            }
            if (streak != null && streak > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.width(6.dp))
                    Text(pluralStringResource(R.plurals.streak_days, streak, streak), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
internal fun ReviewCard(
    number: Int,
    item: ReviewItem,
    language: AppLanguage,
    bookmarked: Boolean,
    onToggleBookmark: () -> Unit
) {
    val q = item.question.localized(language)
    AppCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                val icon = when {
                    item.isCorrect -> Icons.Default.CheckCircle
                    item.selectedIndex == AnswerRecord.TIMED_OUT -> Icons.Default.Timer
                    else -> Icons.Default.Cancel
                }
                Icon(icon, contentDescription = null, tint = if (item.isCorrect) QuizColors.Correct else QuizColors.Wrong)
                Spacer(Modifier.width(8.dp))
                Text("$number. ${q.text}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        if (bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = stringResource(if (bookmarked) R.string.bookmark_remove else R.string.bookmark_add)
                    )
                }
            }
            val yourAnswer = when (item.selectedIndex) {
                AnswerRecord.TIMED_OUT -> stringResource(R.string.review_no_answer)
                else -> q.options.getOrNull(item.selectedIndex) ?: stringResource(R.string.review_no_answer)
            }
            if (!item.isCorrect) {
                Text(
                    stringResource(R.string.review_your_answer, yourAnswer),
                    color = QuizColors.Wrong,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                stringResource(R.string.review_correct_answer, q.options[item.question.answerIndex]),
                color = QuizColors.Correct,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(q.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
