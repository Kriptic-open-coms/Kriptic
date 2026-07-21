package com.kriptic.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KripticDarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = Danger,
)

private val KripticLightColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = Danger,
)

/**
 * Root theme wrapper. Dark mode is the default per docs/04_DESIGN_SYSTEM.md
 * (battery/OLED + night-use considerations), but follows system setting
 * unless [forceDark] is explicitly passed.
 *
 * Usage: wrap your app content once, at the top level:
 *   KripticTheme { KripticApp() }
 */
@Composable
fun KripticTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) KripticDarkColors else KripticLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KripticTypography,
        content = content,
    )
}
