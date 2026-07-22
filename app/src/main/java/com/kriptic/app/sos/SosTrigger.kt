package com.kriptic.app.sos

import com.kriptic.app.mesh.MeshService
import com.kriptic.app.model.BitchatMessage

/**
 * Per docs/01_ARCHITECTURE.md §5: "No confirmation dialog. The trigger is
 * the confirmation." Callers (a long-press gesture, a hardware button
 * combo — exact UI trigger is a product decision, not implemented here)
 * must call [fire] directly with no intermediate "are you sure" screen.
 *
 * RELIABILITY NOTE: docs call for this to send "at maximum relay
 * priority/TTL through the existing mesh pipeline." The inherited
 * MeshService interface (mesh/MeshService.kt) doesn't currently expose a
 * per-message priority/TTL knob — that's a real change to
 * BluetoothMeshService/PacketProcessor internals, which needs the fork's
 * planned security review (docs/05_ROADMAP.md) before being touched. Until
 * that lands, this sends the SOS payload multiple times in a short burst
 * as a cheap reliability stand-in — strictly worse than real TTL/priority
 * handling, and should be replaced once that internal work is done.
 */
class SosTrigger(
    private val meshService: MeshService,
    private val getMyPubKeyHex: () -> String,
    private val getMyNickname: () -> String,
) {
    fun fire(lat: Double?, lon: Double?, burstCount: Int = 3) {
        val payload = SosPayload(
            senderPubKey = getMyPubKeyHex(),
            senderNickname = getMyNickname(),
            lat = lat,
            lon = lon,
            timestamp = System.currentTimeMillis(),
            status = SosStatus.ACTIVE,
        )
        val content = SosPayload.WIRE_PREFIX + payload.toWireJson()
        repeat(burstCount) {
            meshService.sendMessage(content = content, channel = SosPayload.SOS_CHANNEL)
        }
    }

    fun resolve(lat: Double?, lon: Double?) {
        val payload = SosPayload(
            senderPubKey = getMyPubKeyHex(),
            senderNickname = getMyNickname(),
            lat = lat,
            lon = lon,
            timestamp = System.currentTimeMillis(),
            status = SosStatus.RESOLVED,
        )
        meshService.sendMessage(
            content = SosPayload.WIRE_PREFIX + payload.toWireJson(),
            channel = SosPayload.SOS_CHANNEL,
        )
    }

    /** Wire into MeshDelegate.didReceiveMessage alongside MarkerBroadcastBridge. */
    fun handleIncomingMessage(message: BitchatMessage, onSosReceived: (SosPayload) -> Unit): Boolean {
        if (message.channel != SosPayload.SOS_CHANNEL) return false
        val payload = SosPayload.fromWireJson(message.content.removePrefix(SosPayload.WIRE_PREFIX)) ?: return true
        onSosReceived(payload)
        return true
    }
}
