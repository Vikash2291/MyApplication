package com.example.myapplication.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.domain.quiz.AnswerRecord
import com.example.myapplication.domain.quiz.QuizMode
import com.example.myapplication.presentation.ui.components.AnswerOption
import com.example.myapplication.presentation.ui.components.AppCard
import com.example.myapplication.presentation.ui.components.AppTopBar
import com.example.myapplication.presentation.ui.components.EmptyState
import com.example.myapplication.presentation.ui.components.LoadingScreen
import com.example.myapplication.presentation.ui.components.OptionLabels
import com.example.myapplication.presentation.ui.components.OptionState
import com.example.myapplication.presentation.ui.components.PrimaryButton
import com.example.myapplication.presentation.ui.components.QuizColors
import com.example.myapplication.presentation.ui.components.labelRes
import com.example.myapplication.presentation.viewmodel.QuizUiState
import com.example.myapplication.presentation.viewmodel.QuizViewModel

/**
 * The quiz itself. Deliberately has NO banner or any other ad view: answer buttons fill this
 * screen, and placing ads near them would invite accidental clicks. Rewarded videos are only
 * shown when the user explicitly taps a lifeline button, which sits above the question, away
 * from the answers.
 */
@Composable
fun QuizScreen(
    uiState: QuizUiState,
    snackbarHostState: SnackbarHostState,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
    onHint: () -> Unit,
    onExtraTime: () -> Unit,
    onToggleBookmark: () -> Unit,
    onExit: () -> Unit
) {
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    BackHandler { confirmExit = true }

    val title = when (uiState.mode) {
        QuizMode.DAILY -> stringResource(R.string.daily_quiz_title)
        QuizMode.BOOKMARKS -> stringResource(R.string.bookmarks_practice_title)
        QuizMode.CATEGORY -> uiState.category?.let { stringResource(it.labelRes()) } ?: stringResource(R.string.quiz_title)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = title,
                actions = {
                    val q = uiState.current
                    if (q != null) {
                        val bookmarked = q.id in uiState.bookmarkIds
                        IconButton(onClick = onToggleBookmark) {
                            Icon(
                                if (bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = stringResource(if (bookmarked) R.string.bookmark_remove else R.string.bookmark_add)
                            )
                        }
                    }
                    IconButton(onClick = { confirmExit = true }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.quiz_exit))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.padding(padding)) { LoadingScreen() }
            uiState.isEmpty -> Box(Modifier.padding(padding)) {
                EmptyState(
                    icon = Icons.Default.Quiz,
                    title = stringResource(R.string.quiz_empty_title),
                    message = stringResource(
                        if (uiState.mode == QuizMode.BOOKMARKS) R.string.bookmarks_empty_body else R.string.quiz_empty_body
                    )
                )
            }
            else -> QuizContent(uiState, Modifier.padding(padding), onSelect, onNext, onHint, onExtraTime)
        }
    }

    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text(stringResource(R.string.quiz_exit_title)) },
            text = { Text(stringResource(R.string.quiz_exit_body)) },
            confirmButton = {
                TextButton(onClick = { confirmExit = false; onExit() }) { Text(stringResource(R.string.quiz_exit_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmExit = false }) { Text(stringResource(R.string.quiz_exit_continue)) }
            }
        )
    }
}

@Composable
private fun QuizContent(
    s: QuizUiState,
    modifier: Modifier,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
    onHint: () -> Unit,
    onExtraTime: () -> Unit
) {
    val question = s.current ?: return
    val localized = question.localized(s.language)

    Column(modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Progress + timer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.quiz_progress, s.index + 1, s.questions.size),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                val urgent = s.secondsLeft <= 5 && !s.isAnswered
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    tint = if (urgent) QuizColors.Wrong else MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.quiz_seconds, s.secondsLeft),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (urgent) QuizColors.Wrong else MaterialTheme.colorScheme.onSurface
                )
            }
            LinearProgressIndicator(
                progress = { (s.index + if (s.isAnswered) 1 else 0).toFloat() / s.questions.size },
                modifier = Modifier.fillMaxWidth()
            )

            // Opt-in rewarded lifelines, kept above the question and away from the answers.
            if (!s.isAnswered) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onHint,
                        enabled = !s.hintUsed && !s.isAdShowing && s.isRewardedReady,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.lifeline_hint), maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = onExtraTime,
                        enabled = !s.extraTimeUsed && !s.isAdShowing && s.isRewardedReady,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.MoreTime, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.lifeline_extra_time, QuizViewModel.EXTRA_SECONDS), maxLines = 1)
                    }
                }
                Text(
                    stringResource(R.string.lifeline_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AppCard {
                Text(
                    localized.text,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(Modifier.height(4.dp))
            localized.options.forEachIndexed { index, option ->
                val hidden = index in s.hiddenOptions
                if (!hidden) {
                    val state = when {
                        !s.isAnswered -> OptionState.IDLE
                        index == question.answerIndex -> OptionState.CORRECT
                        index == s.selectedIndex -> OptionState.WRONG_SELECTED
                        else -> OptionState.DIMMED
                    }
                    AnswerOption(
                        label = OptionLabels[index],
                        text = option,
                        state = state,
                        enabled = !s.isAnswered && !s.isAdShowing,
                        onClick = { onSelect(index) }
                    )
                }
            }

            if (s.isAnswered) {
                val selected = s.selectedIndex
                val correct = selected == question.answerIndex
                Surface(
                    color = (if (correct) QuizColors.Correct else QuizColors.Wrong).copy(alpha = 0.10f),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            stringResource(
                                when {
                                    correct -> R.string.feedback_correct
                                    selected == AnswerRecord.TIMED_OUT -> R.string.feedback_timeout
                                    else -> R.string.feedback_wrong
                                }
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (correct) QuizColors.Correct else QuizColors.Wrong
                        )
                        if (!correct) {
                            Text(
                                stringResource(R.string.feedback_correct_answer, localized.options[question.answerIndex]),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(localized.explanation, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Next button sits in its own bar under the scrolling content.
        if (s.isAnswered) {
            Surface(tonalElevation = 3.dp) {
                PrimaryButton(
                    text = stringResource(if (s.isLast) R.string.quiz_see_result else R.string.quiz_next),
                    onClick = onNext,
                    loading = s.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                )
            }
        }
    }
}
