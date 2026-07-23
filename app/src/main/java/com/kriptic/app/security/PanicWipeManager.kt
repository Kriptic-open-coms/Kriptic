package com.kriptic.app.security

import android.content.Context
import com.kriptic.app.identity.KripticIdentityRepository
import com.kriptic.app.map.MarkerDatabase

/**
 * Extends (does not replace) bitchat's existing panicClearAllData() in
 * ui/ChatViewModel.kt, per docs/01_ARCHITECTURE.md §6: "Panic wipe: drop
 * and recreate the encrypted database, clear preferences/DataStore, clear
 * the Keystore alias... extends bitchat's existing wipe gesture rather
 * than replacing it."
 *
 * INTEGRATION POINT: call [wipeKripticState] from inside
 * ChatViewModel.panicClearAllData(), alongside the existing mesh/crypto/
 * chat clearing calls it already makes — see the call site comment there.
 * This is intentionally a single synchronous-as-possible entry point with
 * no confirmation step, matching the "panic wipe has no confirmation step,
 * because the point is speed under duress" principle — do not add a
 * confirmation dialog in front of this.
 */
object PanicWipeManager {

    fun wipeKripticState(context: Context) {
        // Marker cache: drop and recreate the encrypted DB file entirely,
        // rather than just deleting rows, so no forensic trace of prior
        // marker data remains in the file itself.
        try {
            MarkerDatabase.destroyInstance(context)
        } catch (_: Exception) { }

        // Kriptic identity (username): clear the locked username so a
        // fresh install-equivalent state exists after wipe. The underlying
        // keypair itself is already handled by bitchat's existing
        // clearAllCryptographicData() — this only clears the Kriptic-added
        // username layer on top of it.
        try {
            KripticIdentityRepository(context).clearForPanicWipe()
        } catch (_: Exception) { }

        // SOS has no persistent local state beyond what's already covered
        // by the mesh/message clearing bitchat's existing wipe performs —
        // nothing additional to clear here today. If SOS state persistence
        // is added later (e.g. an "active SOS" flag), clear it here too.
    }
}
