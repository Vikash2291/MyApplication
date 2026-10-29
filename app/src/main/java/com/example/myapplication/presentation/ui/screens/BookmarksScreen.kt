package com.example.myapplication.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.presentation.ui.components.AppCard
import com.example.myapplication.presentation.ui.components.AppTopBar
import com.example.myapplication.presentation.ui.components.EmptyState
import com.example.myapplication.presentation.ui.components.LoadingScreen
import com.example.myapplication.presentation.ui.components.PrimaryButton
import com.example.myapplication.presentation.ui.components.QuizColors
import com.example.myapplication.presentation.ui.components.labelRes
import com.example.myapplication.presentation.viewmodel.BookmarksUiState

@Composable
fun BookmarksScreen(
    uiState: BookmarksUiState,
    onPractice: () -> Unit,
    onRemove: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(topBar = { AppTopBar(stringResource(R.string.bookmarks_title), onNavigateUp = onBackClick) }) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.padding(padding)) { LoadingScreen() }
            uiState.questions.isEmpty() -> Box(Modifier.padding(padding)) {
                EmptyState(
                    icon = Icons.Default.BookmarkBorder,
                    title = stringResource(R.string.bookmarks_empty_title),
                    message = stringResource(R.string.bookmarks_empty_body)
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PrimaryButton(
                        text = stringResource(R.string.bookmarks_practice),
                        onClick = onPractice,
                        icon = Icons.Default.PlayCircle,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                items(uiState.questions, key = { it.id }) { question ->
                    val q = question.localized(uiState.language)
                    AppCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        stringResource(question.category.labelRes()),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(q.text, style = MaterialTheme.typography.titleSmall)
                                }
                                IconButton(onClick = { onRemove(question.id) }) {
                                    Icon(Icons.Default.Bookmark, contentDescription = stringResource(R.string.bookmark_remove))
                                }
                            }
                            Text(
                                stringResource(R.string.review_correct_answer, q.options[question.answerIndex]),
                                color = QuizColors.Correct,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(q.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
