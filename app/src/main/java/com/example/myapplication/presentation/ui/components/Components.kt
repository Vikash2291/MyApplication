package com.example.myapplication.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.myapplication.presentation.ui.theme.AppTheme

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled && !loading,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
        ),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                androidx.compose.material.ProgressIndicator(
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                icon?.let {
                    Icon(imageVector = it, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
                    androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                }
                Text(text = text, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun OutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
        ),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        border = androidx.compose.ui.graphics.drawscope.Stroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(imageVector = it, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
            }
            Text(text = text, fontWeight = FontWeight.Medium, fontSize = 16.sp)
        }
    }
}

@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )
    ) {
        Text(text = text, fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: androidx.compose.ui.text.input.KeyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default,
    keyboardActions: androidx.compose.ui.text.input.KeyboardActions = androidx.compose.ui.text.input.KeyboardActions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    singleLine: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    trailingIconClick: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = label) },
            placeholder = { Text(text = placeholder) },
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            leadingIcon = leadingIcon?.let { { Icon(imageVector = it, contentDescription = null) } },
            trailingIcon = trailingIcon?.let { {
                IconButton(onClick = trailingIconClick ?: {}) {
                    Icon(imageVector = it, contentDescription = null)
                }
            } },
            colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest,
                focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest,
                unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest,
                disabledContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.12f),
                labelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                placeholderColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                cursorColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
                disabledIndicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f),
                errorContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer,
                errorTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = androidx.compose.material3.MaterialTheme.shapes.medium
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 12.dp)
            )
        }
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    elevation: androidx.compose.material3.CardDefaults.CardElevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp),
    shape: androidx.compose.foundation.shape.Shape = androidx.compose.material3.MaterialTheme.shapes.large,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        elevation = elevation,
        shape = shape,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        content()
    }
}

@Composable
fun LoadingScreen(message: String = "Loading...") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.material.ProgressIndicator(
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionText: String? = null,
    action: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(80.dp)
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            actionText?.let { text ->
                action?.let { onClick ->
                    androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
                    PrimaryButton(text = text, onClick = onClick, modifier = Modifier.width(200.dp))
                }
            }
        }
    }
}

@Composable
fun TopAppBar(
    title: String,
    navigateUp: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    androidx.compose.material3.TopAppBar(
        title = { Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Medium) },
        navigationIcon = navigateUp?.let { {
            IconButton(onClick = it) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                    contentDescription = "Navigate back"
                )
            }
        } },
        actions = actions,
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        )
    )
}