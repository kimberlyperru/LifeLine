package com.perru.lifeline.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LifeLineLightColors = lightColorScheme(
    primary = Terracotta,
    onPrimary = CreamSurface,
    primaryContainer = CrimsonLight,
    onPrimaryContainer = InkBrown,
    secondary = SageGreen,
    onSecondary = CreamSurface,
    secondaryContainer = SageGreenLight,
    onSecondaryContainer = SageGreenDark,
    background = CreamBackground,
    onBackground = InkBrown,
    surface = CreamSurface,
    onSurface = InkBrown,
    surfaceVariant = WarmCardBorder,
    onSurfaceVariant = MutedBrown,
    error = UrgencyCritical,
    outline = Divider
)

private val LifeLineDarkColors = darkColorScheme(
    primary = Terracotta,
    onPrimary = CreamSurface,
    primaryContainer = TerracottaDark,
    onPrimaryContainer = CreamSurface,
    secondary = SageGreen,
    onSecondary = CreamSurface,
    secondaryContainer = SageGreenDark,
    onSecondaryContainer = SageGreenLight,
    background = DarkBackground,
    onBackground = CreamSurface,
    surface = DarkSurface,
    onSurface = CreamSurface,
    surfaceVariant = InkBrown,
    onSurfaceVariant = MutedBrown,
    error = UrgencyCritical,
    outline = MutedBrown
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
            val window = context.window
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // In dark mode, we want light status bar icons (false for appearanceLightStatusBars)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LifeLineTypography,
        content = content
    )
}
