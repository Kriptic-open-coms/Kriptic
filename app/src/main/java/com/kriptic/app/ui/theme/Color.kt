package com.kriptic.app.ui.theme

import androidx.compose.ui.graphics.Color

// Neutrals — Dark theme (default)
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1B1B1E)
val DarkSurfaceElevated = Color(0xFF242428)
val DarkBorder = Color(0xFF313136)
val DarkTextPrimary = Color(0xFFF2F2F2)
val DarkTextSecondary = Color(0xFFA0A0A6)

// Neutrals — Light theme
val LightBackground = Color(0xFFFAFAFA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF2F2F4)
val LightBorder = Color(0xFFE2E2E5)
val LightTextPrimary = Color(0xFF17171A)
val LightTextSecondary = Color(0xFF5C5C63)

// Accent + semantic (single source: DesignTokens)
val Accent = DesignTokens.AccentColor
val AccentPressed = DesignTokens.AccentColorPressed
val Warning = DesignTokens.SemanticWarning
val Danger = DesignTokens.SemanticDanger
val Safe = DesignTokens.SemanticSafe
