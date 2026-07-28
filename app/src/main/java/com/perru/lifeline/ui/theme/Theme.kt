package com.perru.lifeline.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity

private val LifeLineLightColors = lightColorScheme(
    primary = Terracotta, onPrimary = CreamSurface,
    primaryContainer = CrimsonLight, onPrimaryContainer = InkBrown,
    secondary = SageGreen, onSecondary = CreamSurface,
    secondaryContainer = SageGreenLight, onSecondaryContainer = SageGreenDark,
    background = CreamBackground, onBackground = InkBrown,
    surface = CreamSurface, onSurface = InkBrown,
    surfaceVariant = WarmCardBorder, onSurfaceVariant = MutedBrown,
    error = UrgencyCritical, outline = Divider
)

private val LifeLineDarkColors = darkColorScheme(
    primary = Terracotta,
    secondary = SageGreen,
    background = Color(0xFF241C18),
    surface = Color(0xFF2E2420)
)

@Composable
fun LifeLineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LifeLineDarkColors else LifeLineLightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        val context = view.context
        if (context is Activity) {
            WindowCompat.getInsetsController(context.window, view).isAppearanceLightStatusBars = !darkTheme
            context.window.statusBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LifeLineTypography,
        content = content
    )
}