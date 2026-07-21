package com.kriptic.app.mesh

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MeshRouter {

    private val seenMessageIds = LinkedHashSet<String>()
    private val maxSeenBuffer = 1000

    private val _incomingMessages = MutableSharedFlow<MessageEnvelope>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<MessageEnvelope> = _incomingMessages.asSharedFlow()

    private var broadcastSender: ((MessageEnvelope, excludeEndpointId: String?) -> Unit)? = null

    fun registerBroadcastSender(sender: (MessageEnvelope, String?) -> Unit) {
        this.broadcastSender = sender
    }

    suspend fun processIncomingMessage(envelope: MessageEnvelope, fromEndpointId: String? = null) {
        synchronized(seenMessageIds) {
            if (seenMessageIds.contains(envelope.id)) {
                Log.d("MeshRouter", "Dropping duplicate message: ${envelope.id}")
                return
            }
            seenMessageIds.add(envelope.id)
            if (seenMessageIds.size > maxSeenBuffer) {
                val oldest = seenMessageIds.iterator().next()
                seenMessageIds.remove(oldest)
            }
        }

        // Deliver locally
        _incomingMessages.emit(envelope)

        // Relay if TTL > 1
        if (envelope.ttl > 1) {
            val relayed = envelope.copy(ttl = envelope.ttl - 1)
            Log.d("MeshRouter", "Relaying message ${relayed.id} with TTL ${relayed.ttl}")
            broadcastSender?.invoke(relayed, fromEndpointId)
        }
    }

    fun clearHistory() {
        synchronized(seenMessageIds) {
            seenMessageIds.clear()
        }
    }
}
