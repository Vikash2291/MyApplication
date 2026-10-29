package com.example.myapplication.presentation.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.domain.quiz.QuizCategory

@StringRes
fun QuizCategory.labelRes(): Int = when (this) {
    QuizCategory.INDIA_GK -> R.string.category_india_gk
    QuizCategory.WORLD_GK -> R.string.category_world_gk
    QuizCategory.SCIENCE -> R.string.category_science
    QuizCategory.HISTORY -> R.string.category_history
    QuizCategory.GEOGRAPHY -> R.string.category_geography
    QuizCategory.SPORTS -> R.string.category_sports
    QuizCategory.CURRENT_AFFAIRS -> R.string.category_current_affairs
    QuizCategory.COMPUTERS -> R.string.category_computers
}

fun QuizCategory.icon(): ImageVector = when (this) {
    QuizCategory.INDIA_GK -> Icons.Default.Flag
    QuizCategory.WORLD_GK -> Icons.Default.Public
    QuizCategory.SCIENCE -> Icons.Default.Science
    QuizCategory.HISTORY -> Icons.Default.HistoryEdu
    QuizCategory.GEOGRAPHY -> Icons.Default.Landscape
    QuizCategory.SPORTS -> Icons.Default.SportsCricket
    QuizCategory.CURRENT_AFFAIRS -> Icons.Default.Newspaper
    QuizCategory.COMPUTERS -> Icons.Default.Computer
}

/** Fixed, colour-blind-friendly feedback colours that read well in light and dark themes. */
object QuizColors {
    val Correct = Color(0xFF2E7D32)
    val Wrong = Color(0xFFC62828)
}

enum class OptionState { IDLE, CORRECT, WRONG_SELECTED, DIMMED }

/** One answer option. Large touch target with clear spacing to avoid mis-taps. */
@Composable
fun AnswerOption(
    label: String,
    text: String,
    state: OptionState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (state) {
        OptionState.CORRECT -> QuizColors.Correct
        OptionState.WRONG_SELECTED -> QuizColors.Wrong
        else -> MaterialTheme.colorScheme.outline
    }
    val container = when (state) {
        OptionState.CORRECT -> QuizColors.Correct.copy(alpha = 0.12f)
        OptionState.WRONG_SELECTED -> QuizColors.Wrong.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surface
    }
    val contentAlpha = if (state == OptionState.DIMMED) 0.6f else 1f
    OutlinedCard(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(if (state == OptionState.IDLE || state == OptionState.DIMMED) 1.dp else 2.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(
            containerColor = container,
            disabledContainerColor = container
        )
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = contentAlpha),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                modifier = Modifier.weight(1f)
            )
            when (state) {
                OptionState.CORRECT -> Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.cd_correct_answer),
                    tint = QuizColors.Correct
                )
                OptionState.WRONG_SELECTED -> Icon(
                    Icons.Default.Cancel,
                    contentDescription = stringResource(R.string.cd_wrong_answer),
                    tint = QuizColors.Wrong
                )
                else -> Unit
            }
        }
    }
}

@Composable
fun StatTile(value: String, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.size(8.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

/** Option letters shown next to answers (A–D in both languages for familiarity). */
val OptionLabels = listOf("A", "B", "C", "D")
