package com.kriptic.app.identity

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature

data class Identity(
    val username: String,
    val publicKey: ByteArray,
    val signature: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Identity) return false
        return username == other.username &&
               publicKey.contentEquals(other.publicKey) &&
               signature.contentEquals(other.signature)
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}

class IdentityRepository(private val context: Context) {

    private val keystoreAlias = "kriptic_identity_key"
    private val prefsName = "kriptic_identity"
    private val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun createIdentity(username: String): Identity {
        val keyPair = generateKeyPair()
        val signature = signUsername(username, keyPair.private)

        prefs.edit()
            .putString("username", username)
            .putString("public_key", android.util.Base64.encodeToString(keyPair.public.encoded, android.util.Base64.NO_WRAP))
            .putString("signature", android.util.Base64.encodeToString(signature, android.util.Base64.NO_WRAP))
            .putBoolean("has_identity", true)
            .apply()

        return Identity(
            username = username,
            publicKey = keyPair.public.encoded,
            signature = signature
        )
    }

    fun getIdentity(): Identity? {
        if (!prefs.getBoolean("has_identity", false)) return null
        val username = prefs.getString("username", null) ?: return null
        val publicKeyEncoded = prefs.getString("public_key", null) ?: return null
        val signatureEncoded = prefs.getString("signature", null) ?: return null

        return Identity(
            username = username,
            publicKey = android.util.Base64.decode(publicKeyEncoded, android.util.Base64.NO_WRAP),
            signature = android.util.Base64.decode(signatureEncoded, android.util.Base64.NO_WRAP)
        )
    }

    fun hasIdentity(): Boolean = prefs.getBoolean("has_identity", false)

    fun destroyIdentity() {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        ks.deleteEntry(keystoreAlias)
        prefs.edit().clear().apply()
    }

    private fun generateKeyPair(): KeyPair {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)

        if (ks.containsAlias(keystoreAlias)) {
            val entry = ks.getEntry(keystoreAlias, null) as KeyStore.PrivateKeyEntry
            return KeyPair(entry.certificate.publicKey, entry.privateKey)
        }

        val spec = KeyGenParameterSpec.Builder(
            keystoreAlias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(java.security.spec.ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setKeySize(256)
            .build()

        val generator = KeyPairGenerator.getInstance("EC", "AndroidKeyStore")
        generator.initialize(spec)
        return generator.generateKeyPair()
    }

    private fun signUsername(username: String, privateKey: java.security.PrivateKey): ByteArray {
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        signature.update(username.toByteArray())
        return signature.sign()
    }
}
