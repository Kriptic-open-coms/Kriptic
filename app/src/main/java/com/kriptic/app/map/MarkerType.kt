package com.kriptic.app.map

import androidx.compose.ui.graphics.Color
import com.kriptic.app.ui.theme.Accent
import com.kriptic.app.ui.theme.Danger
import com.kriptic.app.ui.theme.Gather
import com.kriptic.app.ui.theme.Safe
import com.kriptic.app.ui.theme.Warning

/**
 * The extensible marker-type set from docs/01_ARCHITECTURE.md §4 and the
 * mockups. Colors are semantic (NOT the accent color), per
 * docs/04_DESIGN_SYSTEM.md, except Help which deliberately borrows the
 * accent since it's the same "direct request for help" family as SOS.
 */
enum class MarkerType(val label: String, val color: Color, val defaultExpiryMinutes: Long) {
    DANGER("Danger", Danger, 90),
    SAFE("Safe", Safe, 180),
    POLICE("Police", Warning, 60),
    HELP("Help", Accent, 90),
    GATHER("Gather", Gather, 240);

    companion object {
        fun fromWireValue(value: String): MarkerType? = entries.firstOrNull { it.name == value }
    }
}
