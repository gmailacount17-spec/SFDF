package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DepthXColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color(0xFF03221B),
    primaryContainer = Color(0xFF00382E),
    onPrimaryContainer = CyanPrimary,
    secondary = BlueSecondary,
    onSecondary = Color(0xFF001F2B),
    secondaryContainer = Color(0xFF00354A),
    onSecondaryContainer = BlueSecondary,
    tertiary = VioletTertiary,
    onTertiary = Color(0xFF380036),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force modern dark mode for live wallpaper studio
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DarkBackground.toArgb()
                window.navigationBarColor = DarkBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DepthXColorScheme,
        typography = Typography,
        content = content
    )
}
