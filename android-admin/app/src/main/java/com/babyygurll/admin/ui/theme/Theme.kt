package com.babyygurll.admin.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandSurface,
    primaryContainer = BrandPrimaryLight,
    onPrimaryContainer = BrandSecondary,
    secondary = BrandSecondary,
    onSecondary = BrandSurface,
    secondaryContainer = BrandPrimaryLight,
    onSecondaryContainer = BrandSecondary,
    tertiary = BrandAccentRed,
    onTertiary = BrandSurface,
    background = BrandBackground,
    onBackground = TextPrimary,
    surface = BrandSurface,
    onSurface = TextPrimary,
    outline = BrandBorder,
    error = BrandAccentRed
)

@Composable
fun BabyyGurllAdminTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BrandSecondary.toArgb()
            window.navigationBarColor = BrandSecondary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

