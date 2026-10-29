package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ClaudeTerracotta,
    onPrimary = ClaudeIvorySurface,
    primaryContainer = ClaudeTerracottaContainer,
    onPrimaryContainer = ClaudeTerracottaHover,
    secondary = ClaudeIvoryTextSecondary,
    onSecondary = ClaudeIvorySurface,
    background = ClaudeIvoryBg,
    onBackground = ClaudeIvoryText,
    surface = ClaudeIvorySurface,
    onSurface = ClaudeIvoryText,
    surfaceVariant = ClaudeIvorySurfaceVariant,
    onSurfaceVariant = ClaudeIvoryTextSecondary,
    outline = ClaudeIvoryBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = ClaudeTerracottaDark,
    onPrimary = ClaudeDarkBg,
    primaryContainer = ClaudeTerracottaContainerDark,
    onPrimaryContainer = ClaudeTerracottaDark,
    secondary = ClaudeDarkTextSecondary,
    onSecondary = ClaudeDarkBg,
    background = ClaudeDarkBg,
    onBackground = ClaudeDarkText,
    surface = ClaudeDarkSurface,
    onSurface = ClaudeDarkText,
    surfaceVariant = ClaudeDarkSurfaceVariant,
    onSurfaceVariant = ClaudeDarkTextSecondary,
    outline = ClaudeDarkBorder
)

@Composable
fun ClaudeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
