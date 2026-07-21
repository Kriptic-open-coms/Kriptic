package com.kriptic.app.security

import android.content.Context
import android.util.Log
import com.kriptic.app.mesh.MeshRouter
import java.security.KeyStore

class PanicWipeManager(private val meshRouter: MeshRouter) {

    fun executePanicWipe(context: Context, onWipeComplete: () -> Unit) {
        Log.w("PanicWipe", "PANIC WIPE INITIATED — DESTROYING ALL LOCAL DATA")

        try {
            // 1. Clear SharedPreferences
            val prefs = context.getSharedPreferences("kriptic_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            // 2. Clear KeyStore Alias
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (keyStore.containsAlias("kriptic_identity_key")) {
                keyStore.deleteEntry("kriptic_identity_key")
            }

            // 3. Clear Room Database Files
            context.deleteDatabase("kriptic_db")

            // 4. Reset Mesh Deduplication History
            meshRouter.clearHistory()

            Log.w("PanicWipe", "PANIC WIPE COMPLETE")
            onWipeComplete()
        } catch (e: Exception) {
            Log.e("PanicWipe", "Error during panic wipe: ${e.message}", e)
            onWipeComplete()
        }
    }
}
