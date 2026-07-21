package com.kriptic.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * DESIGN TOKENS — the single place to configure Kriptic's visual identity.
 */
object DesignTokens {

    // ------------------------------------------------------------------
    // ACCENT COLOR — the single brand/action color used across the app.
    // Default: a clear, confident blue. Change this one hex to reskin.
    // ------------------------------------------------------------------
    val AccentColor = Color(0xFF4F7CFF)
    val AccentColorPressed = Color(0xFF3D63D9)

    // Semantic colors
    val SemanticWarning = Color(0xFFF5A623)   // police lines, barricades
    val SemanticDanger = Color(0xFFE5484D)    // active danger / SOS escalation only
    val SemanticSafe = Color(0xFF3DAA6B)      // safe zones, legal aid, medical

    // ------------------------------------------------------------------
    // FONT — modern clean sans-serif typeface (Inter / System Default)
    // ------------------------------------------------------------------
    val AppFontFamily = FontFamily.Default

    // ------------------------------------------------------------------
    // SPACING SCALE (8dp base unit)
    // ------------------------------------------------------------------
    object Spacing {
        val xs = 4
        val sm = 8
        val md = 16
        val lg = 24
        val xl = 32
        val xxl = 48
    }

    // ------------------------------------------------------------------
    // CORNER RADIUS SCALE
    // ------------------------------------------------------------------
    object Radius {
        val small = 8
        val medium = 14
        val large = 24
    }
}
