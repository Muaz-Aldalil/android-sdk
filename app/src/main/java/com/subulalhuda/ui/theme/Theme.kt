package com.subulalhuda.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Material3 color scheme mapping from website CSS tokens.
 *
 * Primary = text color (primary text)
 * Secondary = accent (gold)
 * Background = body background
 * Surface = card/section backgrounds
 * OnPrimary = text on accent backgrounds
 */
private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = SurfaceLight,
    primaryContainer = AccentLight,
    onPrimaryContainer = PrimaryLight,

    secondary = AccentLight,
    onSecondary = PrimaryLight,
    secondaryContainer = AccentHoverLight,
    onSecondaryContainer = PrimaryLight,

    tertiary = SuccessLight,
    onTertiary = SurfaceLight,

    background = BackgroundLight,
    onBackground = PrimaryLight,

    surface = SurfaceLight,
    onSurface = PrimaryLight,
    surfaceVariant = SurfaceAltLight,
    onSurfaceVariant = TextSecondaryLight,

    error = ErrorLight,
    onError = SurfaceLight,

    outline = BorderLight,
    outlineVariant = TextMutedLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = SurfaceDark,
    primaryContainer = AccentDark,
    onPrimaryContainer = PrimaryDark,

    secondary = AccentDark,
    onSecondary = PrimaryDark,
    secondaryContainer = AccentHoverDark,
    onSecondaryContainer = PrimaryDark,

    tertiary = SuccessDark,
    onTertiary = PrimaryDark,

    background = BackgroundDark,
    onBackground = PrimaryDark,

    surface = SurfaceDark,
    onSurface = PrimaryDark,
    surfaceVariant = SurfaceAltDark,
    onSurfaceVariant = TextSecondaryDark,

    error = ErrorDark,
    onError = PrimaryDark,

    outline = BorderDark,
    outlineVariant = TextMutedDark,
)

/**
 * App theme.
 *
 * @param darkTheme When true, uses dark color scheme. Defaults to system setting.
 * @param content Composable content.
 */
@Composable
fun SubulTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Set status bar and navigation bar colors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SubulTypography,
        content = content,
    )
}
