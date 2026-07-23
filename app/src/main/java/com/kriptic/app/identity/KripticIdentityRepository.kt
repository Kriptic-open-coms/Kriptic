package com.kriptic.app.identity

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Kriptic's identity layer on top of the inherited [SecureIdentityStateManager]
 * keypair storage (see docs/01_ARCHITECTURE.md §1).
 *
 * Rules, enforced here rather than just in the UI:
 * - A username can be set exactly once per install.
 * - Once set, it cannot be changed from within the app — the only way to
 *   get a new username is a reinstall (or panic wipe, which resets identity
 *   entirely). This is a deliberate anti-spoofing/anti-rotation-abuse
 *   choice per docs/01_ARCHITECTURE.md §1, not an oversight.
 * - Uniqueness is NOT globally guaranteed here — only checked against
 *   currently-visible mesh peers at registration time, per the documented
 *   v1 limitation. The registration UI must say this plainly.
 */
class KripticIdentityRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "kriptic_identity"
        private const val KEY_USERNAME = "kriptic_username"
        private const val KEY_USERNAME_LOCKED = "kriptic_username_locked"
    }

    private val prefs = run {
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Null until the user has completed registration. */
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun isRegistered(): Boolean = prefs.getBoolean(KEY_USERNAME_LOCKED, false) && getUsername() != null

    /**
     * Attempts to set the username. Returns false (no-op) if a username is
     * already locked in — the caller should never expose a path to call
     * this twice, but this guard makes that invariant hold even if it does.
     */
    fun setUsernameOnce(username: String): Boolean {
        if (isRegistered()) return false
        val trimmed = username.trim()
        if (!isValidUsername(trimmed)) return false
        prefs.edit()
            .putString(KEY_USERNAME, trimmed)
            .putBoolean(KEY_USERNAME_LOCKED, true)
            .apply()
        return true
    }

    /** Used only by panic wipe / full identity reset — see security/PanicWipeManager.kt. */
    fun clearForPanicWipe() {
        prefs.edit().clear().apply()
    }

    fun isValidUsername(username: String): Boolean {
        // 3-20 chars, alphanumeric + underscore, must start with a letter —
        // deliberately simple: this is a display handle, not a security
        // boundary (that's the keypair's job).
        return Regex("^[A-Za-z][A-Za-z0-9_]{2,19}$").matches(username)
    }

    /**
     * Local-mesh-session uniqueness check against currently visible peer
     * nicknames. This is NOT a global uniqueness guarantee — see class doc.
     */
    fun isTakenByVisiblePeer(username: String, visiblePeerNicknames: Collection<String>): Boolean {
        return visiblePeerNicknames.any { it.equals(username, ignoreCase = true) }
    }
}
