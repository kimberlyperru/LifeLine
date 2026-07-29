package com.perru.lifeline.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity

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

/**
 * LifeLine is deliberately a light, warm brand (cream/terracotta/sage) — it does
 * NOT follow the system dark-mode setting. Auto-switching to dark previously left
 * many components on Material's generic dark defaults (since only a handful of
 * roles were customized for dark mode), which read as a muddy, unbranded dark
 * screen. Always rendering light keeps every screen on-brand and legible.
 */
@Composable
fun LifeLineTheme(content: @Composable () -> Unit) {
    val colorScheme = LifeLineLightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        val context = view.context
        if (context is Activity) {
            WindowCompat.getInsetsController(context.window, view).isAppearanceLightStatusBars = true
            context.window.statusBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LifeLineTypography,
        content = content
    )
}
