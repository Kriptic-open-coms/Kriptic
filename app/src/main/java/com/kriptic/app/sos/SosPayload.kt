package com.kriptic.app.sos

import org.json.JSONObject

/**
 * {senderPubKey, lat, lon, timestamp, status} per docs/01_ARCHITECTURE.md §5.
 * Reuses the same channel-message envelope trick as markers (see
 * map/Marker.kt) rather than a new binary packet type.
 */
data class SosPayload(
    val senderPubKey: String,
    val senderNickname: String,
    val lat: Double?,
    val lon: Double?,
    val timestamp: Long,
    val status: SosStatus,
) {
    fun toWireJson(): String = JSONObject().apply {
        put("senderPubKey", senderPubKey)
        put("senderNickname", senderNickname)
        put("lat", lat)
        put("lon", lon)
        put("timestamp", timestamp)
        put("status", status.name)
    }.toString()

    companion object {
        const val WIRE_PREFIX = "KRIPTIC_SOS:"
        const val SOS_CHANNEL = "#__kriptic_sos"

        fun fromWireJson(json: String): SosPayload? = try {
            val o = JSONObject(json)
            SosPayload(
                senderPubKey = o.getString("senderPubKey"),
                senderNickname = o.optString("senderNickname", "unknown"),
                lat = if (o.isNull("lat")) null else o.getDouble("lat"),
                lon = if (o.isNull("lon")) null else o.getDouble("lon"),
                timestamp = o.getLong("timestamp"),
                status = SosStatus.entries.firstOrNull { it.name == o.getString("status") } ?: SosStatus.ACTIVE,
            )
        } catch (e: Exception) {
            null
        }
    }
}

enum class SosStatus { ACTIVE, RESOLVED }
