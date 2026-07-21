package com.kriptic.app.mesh

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class MessageType {
    CHAT,
    HAZARD_PIN,
    SOS,
    ACK
}

@Serializable
data class MessageEnvelope(
    val id: String = UUID.randomUUID().toString(),
    val senderPubKey: String,
    val recipientPubKey: String? = null, // null indicates broadcast to all peers
    val ttl: Int = 5,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.CHAT,
    val payloadEncrypted: String, // Base64 encoded payload
    val signature: String = "" // Base64 signature for authenticity
)
