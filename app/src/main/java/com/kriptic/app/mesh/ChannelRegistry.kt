package com.kriptic.app.mesh

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kriptic.app.ui.theme.Accent
import com.kriptic.app.ui.theme.Caption

/**
 * Kriptic's default channel set, per docs/01_ARCHITECTURE.md §2 and the
 * mockups. Config-driven (a plain list, not hardcoded per-screen) so
 * adding a region- or event-specific channel later is a one-line change
 * here rather than a UI rewrite.
 *
 * These map directly onto the inherited bitchat [ChannelManager]'s
 * name-based join model (`#general` etc.) — no password, world-joinable,
 * auto-joined for every user on first mesh connection.
 */
data class KripticChannel(val id: String, val displayName: String)

object ChannelRegistry {
    val defaultChannels = listOf(
        KripticChannel("#general", "General"),
        KripticChannel("#priority", "Priority"),
        KripticChannel("#danger-alert", "Danger/Alert"),
        KripticChannel("#information", "Information"),
    )

    /**
     * Call once on startup so every Kriptic install lands in all four
     * default channels without the user having to manually join anything —
     * matches the "no coming-soon, everything real and on by default"
     * scope rule. Takes a plain join callback (e.g.
     * `chatViewModel::joinChannel`) rather than the internal ChannelManager
     * type directly, since that's a private field on ChatViewModel — see
     * ui/nav/KripticAppScaffold.kt for the call site.
     */
    fun joinAllDefaults(join: (channelId: String) -> Unit) {
        defaultChannels.forEach { channel -> join(channel.id) }
    }
}

/**
 * Shared pill-chip row — same component/visual treatment used on both the
 * Messaging tab (channel switch) and the Knowledge tab (topic filter), per
 * docs/04_DESIGN_SYSTEM.md ("shared component reused across Messaging and
 * Knowledge, so the app reads as one coherent tagging system").
 */
@Composable
fun KripticChipRow(
    items: List<Pair<String, String>>, // (id, label)
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
    ) {
        items(items) { (id, label) ->
            val selected = id == selectedId
            FilterChip(
                selected = selected,
                onClick = { onSelect(id) },
                label = { Text(label, style = Caption) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Accent,
                ),
            )
        }
    }
}
