package com.example.myapplication.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.myapplication.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF0064B3),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD0E3FF),
    onPrimaryContainer = Color(0xFF001C3A),
    secondary = Color(0xFF4A6375),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDE5F9),
    onSecondaryContainer = Color(0xFF051E2D),
    tertiary = Color(0xFF006474),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFAAE9FF),
    onTertiaryContainer = Color(0xFF001F25),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFF9F9FC),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFF9F9FC),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC3C6CF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FCAFF),
    onPrimary = Color(0xFF00325E),
    primaryContainer = Color(0xFF004985),
    onPrimaryContainer = Color(0xFFD0E3FF),
    secondary = Color(0xFFB1CADD),
    onSecondary = Color(0xFF1C3343),
    secondaryContainer = Color(0xFF33495A),
    onSecondaryContainer = Color(0xFFCDE5F9),
    tertiary = Color(0xFF82D2E8),
    onTertiary = Color(0xFF00363F),
    tertiaryContainer = Color(0xFF004E5B),
    onTertiaryContainer = Color(0xFFAAE9FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF111316),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF111316),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C6CF),
    outline = Color(0xFF8D9199),
    outlineVariant = Color(0xFF43474E)
)

private val AppTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
