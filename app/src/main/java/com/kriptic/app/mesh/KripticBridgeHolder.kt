package com.kriptic.app.mesh

import com.kriptic.app.model.BitchatMessage

/**
 * MarkerBroadcastBridge and SosTrigger are constructed at the Compose/UI
 * layer (see ui/nav/KripticAppScaffold.kt), but the incoming-message hook
 * they need lives inside MeshDelegateHandler, which is constructed earlier
 * as part of ChatViewModel's init block. Rather than threading two new
 * constructor parameters through ChatViewModel's already-large init graph
 * (real surgery, higher risk of an unrelated regression in inherited code
 * this pass didn't otherwise touch), this holder follows the same static-
 * singleton pattern the fork already uses for MeshServiceHolder.
 *
 * KripticAppScaffold sets [handler] once on first composition. If it
 * returns true, the message was a Kriptic marker/SOS payload and
 * MeshDelegateHandler should NOT also render it as a normal chat message.
 */
object KripticBridgeHolder {
    @Volatile
    var handler: ((BitchatMessage) -> Boolean)? = null

    fun handle(message: BitchatMessage): Boolean = handler?.invoke(message) ?: false
}
