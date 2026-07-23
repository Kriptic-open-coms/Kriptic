package com.kriptic.app.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

/**
 * Kriptic's color schemes — calm, quiet, grayscale-first per
 * docs/04_DESIGN_SYSTEM.md. Dark mode is the default (battery/OLED +
 * night/protest-use considerations); light mode follows the same tokens.
 */
private val KripticDarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = Danger,
    onError = androidx.compose.ui.graphics.Color.White,
)

private val KripticLightColors = lightColorScheme(
    primary = Accent,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = Danger,
    onError = androidx.compose.ui.graphics.Color.White,
)

/**
 * Root theme wrapper. Respects the user's stored ThemePreference
 * (System / Dark / Light) inherited from the bitchat fork, defaulting to
 * dark when following the system and the system reports dark.
 *
 * Usage: wrap your app content once, at the top level:
 *   KripticTheme { KripticApp() }
 */
@Composable
fun KripticTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val themePref by ThemePreferenceManager.themeFlow.collectAsState(initial = ThemePreference.System)
    val shouldUseDark = when (darkTheme) {
        true -> true
        false -> false
        null -> when (themePref) {
            ThemePreference.Dark -> true
            ThemePreference.Light -> false
            ThemePreference.System -> isSystemInDarkTheme()
        }
    }

    val colorScheme = if (shouldUseDark) KripticDarkColors else KripticLightColors

    val view = LocalView.current
    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    if (!shouldUseDark) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = if (!shouldUseDark) {
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else 0
            }
            window.navigationBarColor = colorScheme.background.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KripticTypography,
        content = content,
    )
}
