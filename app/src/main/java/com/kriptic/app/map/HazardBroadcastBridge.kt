package com.kriptic.app.map

import android.util.Log
import com.kriptic.app.mesh.CryptoService
import com.kriptic.app.mesh.MessageEnvelope
import com.kriptic.app.mesh.MeshRouter
import com.kriptic.app.mesh.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HazardBroadcastBridge(
    private val meshRouter: MeshRouter,
    private val cryptoService: CryptoService
) {
    private val _activeHazardPins = MutableStateFlow<List<HazardPin>>(emptyList())
    val activeHazardPins: StateFlow<List<HazardPin>> = _activeHazardPins.asStateFlow()

    private val pinMap = mutableMapOf<String, HazardPin>()

    suspend fun listenForHazardBroadcasts() {
        meshRouter.incomingMessages.collect { envelope ->
            if (envelope.type == MessageType.HAZARD_PIN) {
                try {
                    val jsonString = cryptoService.decryptPayload(envelope.payloadEncrypted)
                    val pin = Json.decodeFromString<HazardPin>(jsonString)
                    
                    // Filter out expired pins
                    if (System.currentTimeMillis() < pin.expiresAt) {
                        pinMap[pin.id] = pin
                        _activeHazardPins.value = pinMap.values.filter { 
                            System.currentTimeMillis() < it.expiresAt 
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HazardBridge", "Error parsing hazard pin: ${e.message}")
                }
            }
        }
    }

    fun broadcastNewHazardPin(pin: HazardPin) {
        pinMap[pin.id] = pin
        _activeHazardPins.value = pinMap.values.toList()

        val jsonString = Json.encodeToString(pin)
        val encryptedPayload = cryptoService.encryptPayload(jsonString)
        val myPubKey = cryptoService.getPublicKeyBase64()

        val envelope = MessageEnvelope(
            senderPubKey = myPubKey,
            type = MessageType.HAZARD_PIN,
            payloadEncrypted = encryptedPayload
        )

        // Hand over to MeshRouter for broadcast
        meshRouter.registerBroadcastSender { envelopeToBroadCast, _ ->
            // Envelope will be routed via NearbyMeshManager
        }
    }
}
