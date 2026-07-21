package com.kriptic.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/**
 * DESIGN TOKENS — the single place to configure Kriptic's visual identity.
 *
 * Change [AccentColor] or [AppFontFamily] here and the entire app updates.
 * Do not hardcode colors or fonts anywhere else in the codebase — always
 * reference these tokens (directly, or via Color.kt / Type.kt which derive
 * from them) so the app stays reskinnable from one file.
 */
object DesignTokens {

    // ------------------------------------------------------------------
    // ACCENT COLOR — the single brand/action color used across the app.
    // Default: a clear, confident blue. Change this one hex to reskin.
    // ------------------------------------------------------------------
    val AccentColor = Color(0xFF4F7CFF)

    // Optional secondary tone derived from the accent for pressed/hover
    // states. Kept as a separate token in case you want manual control
    // instead of relying on Compose's default alpha/darken behavior.
    val AccentColorPressed = Color(0xFF3D63D9)

    // Semantic colors — intentionally NOT tied to AccentColor, since these
    // carry specific meaning (hazard map pins, status indicators) and
    // should stay legible/distinct even if AccentColor is changed.
    val SemanticWarning = Color(0xFFF5A623)   // police lines, barricades
    val SemanticDanger = Color(0xFFE5484D)    // active danger / SOS escalation only
    val SemanticSafe = Color(0xFF3DAA6B)      // safe zones, legal aid, medical

    // ------------------------------------------------------------------
    // FONT — the single typeface used across the app.
    // Default: Inter. To change, drop new .ttf files into res/font/ and
    // update the FontFamily below — nothing else needs to change.
    //
    // NOTE: To use Inter, download the .ttf files from
    // https://fonts.google.com/specimen/Inter and place in res/font/ as:
    //   inter_regular.ttf, inter_medium.ttf, inter_semibold.ttf
    // Then uncomment the FontFamily below and remove this line.
    // For now, uses the system default font (Roboto on most Androids).

    val AppFontFamily = FontFamily.Default

    // ------------------------------------------------------------------
    // SPACING SCALE (8dp base unit) — reference these instead of magic numbers
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
