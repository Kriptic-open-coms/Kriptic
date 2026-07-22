package com.kriptic.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type scale — see docs/04_DESIGN_SYSTEM.md.
 * Only 5 sizes used across the entire app; do not introduce ad hoc sizes
 * in individual screens. All styles use DesignTokens.AppFontFamily so a
 * font swap in DesignTokens.kt updates everything here automatically.
 */

private val font = DesignTokens.AppFontFamily

const val BASE_FONT_SIZE = 15

val Display = TextStyle(fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 38.sp)
val Title = TextStyle(fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp)
val Heading = TextStyle(fontFamily = font, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 22.sp)
val Body = TextStyle(fontFamily = font, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp)
val Caption = TextStyle(fontFamily = font, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp)

// Maps our custom scale onto Material 3's Typography slots so standard
// Material components (buttons, text fields, nav labels, etc.) pick up
// the right font/sizes automatically without per-component overrides.
val KripticTypography = Typography(
    displaySmall = Display,
    titleLarge = Title,
    titleMedium = Heading,
    titleSmall = Heading,
    bodyLarge = Body,
    bodyMedium = Body,
    bodySmall = Caption,
    labelLarge = Body,
    labelMedium = Caption,
    labelSmall = Caption,
)
