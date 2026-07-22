package com.kriptic.app.map

import com.kriptic.app.mesh.MeshService
import com.kriptic.app.model.BitchatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Markers reuse the existing mesh envelope/relay pipeline (per
 * docs/01_ARCHITECTURE.md §4) instead of a new binary packet type: a
 * marker is sent as a plain channel message on a reserved, hidden channel,
 * which already gets bitchat's store-and-forward relay, TTL, and dedup for
 * free. The UI never shows this channel in the normal channel switcher.
 */
class MarkerBroadcastBridge(
    private val meshService: MeshService,
    private val repository: MarkerRepository,
    private val scope: CoroutineScope,
) {
    companion object {
        const val MARKER_CHANNEL = "#__kriptic_markers"
    }

    fun broadcastMarker(marker: Marker) {
        meshService.sendMessage(
            content = Marker.WIRE_PREFIX + marker.toWireJson(),
            channel = MARKER_CHANNEL,
        )
        // Also ingest locally immediately so the reporter sees their own pin
        // without waiting on a mesh round-trip.
        scope.launch { repository.ingestAutoCorroborate(marker) }
    }

    /**
     * Wire this into the app's MeshDelegate.didReceiveMessage implementation
     * (see ui/MeshDelegateHandler.kt) — if a message arrives on the marker
     * channel, parse and ingest it instead of showing it as chat.
     * Returns true if the message was a marker and was handled (caller
     * should not also render it as a normal chat message).
     */
    fun handleIncomingMessage(message: BitchatMessage): Boolean {
        if (message.channel != MARKER_CHANNEL) return false
        val payload = message.content.removePrefix(Marker.WIRE_PREFIX)
        val marker = Marker.fromWireJson(payload) ?: return true // consumed but malformed
        scope.launch { repository.ingestAutoCorroborate(marker) }
        return true
    }
}
