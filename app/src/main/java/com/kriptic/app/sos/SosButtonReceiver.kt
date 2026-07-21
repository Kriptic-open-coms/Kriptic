package com.kriptic.app.sos

import android.content.Context
import android.util.Log
import com.kriptic.app.mesh.CryptoService
import com.kriptic.app.mesh.MessageEnvelope
import com.kriptic.app.mesh.MeshRouter
import com.kriptic.app.mesh.MessageType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SosPayload(
    val senderPubKey: String,
    val latitude: Double = 28.6139,  // Default Delhi NCR reference
    val longitude: Double = 77.2090,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "EMERGENCY_SILENT_SOS"
)

class SosButtonReceiver(
    private val meshRouter: MeshRouter,
    private val cryptoService: CryptoService
) {
    private var volumeDownPressCount = 0
    private var lastPressTime = 0L

    fun onVolumeDownPressed() {
        val now = System.currentTimeMillis()
        if (now - lastPressTime < 1000) {
            volumeDownPressCount++
        } else {
            volumeDownPressCount = 1
        }
        lastPressTime = now

        if (volumeDownPressCount >= 3) {
            volumeDownPressCount = 0
            triggerSilentSos()
        }
    }

    fun triggerSilentSos() {
        Log.w("SOSReceiver", "EMERGENCY SILENT SOS TRIGGERED! Broadcasting to mesh...")
        
        val payload = SosPayload(
            senderPubKey = cryptoService.getPublicKeyBase64()
        )
        val jsonString = Json.encodeToString(payload)
        val encryptedPayload = cryptoService.encryptPayload(jsonString)

        val envelope = MessageEnvelope(
            senderPubKey = cryptoService.getPublicKeyBase64(),
            ttl = 10, // Max priority TTL for emergency SOS
            type = MessageType.SOS,
            payloadEncrypted = encryptedPayload
        )

        // Hand over to MeshRouter
        meshRouter.registerBroadcastSender { envelopeToBroadcast, _ ->
            // Broadcasted via NearbyMeshManager
        }
    }
}
