package com.kriptic.app.map

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject
import java.util.UUID

/**
 * Marker { lat, lon, type, description, timestamp, reporterPubKey, expiresAt }
 * — as specified in docs/01_ARCHITECTURE.md §4.
 *
 * Broadcast as a plain JSON string over the existing mesh channel envelope
 * (see MarkerBroadcastBridge) rather than a new binary packet type — this
 * reuses bitchat's already-audited channel message relay/dedup/TTL path
 * instead of adding a second wire format to the inherited mesh core.
 */
@Entity(tableName = "markers")
data class Marker(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val lat: Double,
    val lon: Double,
    val type: MarkerType,
    val description: String,
    val timestamp: Long,
    val reporterPubKey: String,
    val expiresAt: Long,
    /** How many distinct reporters have corroborated this same marker — see MarkerRepository. */
    val corroborationCount: Int = 1,
) {
    fun isExpired(nowMs: Long = System.currentTimeMillis()): Boolean = nowMs >= expiresAt

    fun toWireJson(): String = JSONObject().apply {
        put("id", id)
        put("lat", lat)
        put("lon", lon)
        put("type", type.name)
        put("description", description)
        put("timestamp", timestamp)
        put("reporterPubKey", reporterPubKey)
        put("expiresAt", expiresAt)
    }.toString()

    companion object {
        const val WIRE_PREFIX = "KRIPTIC_MARKER:"

        fun fromWireJson(json: String): Marker? {
            return try {
                val o = JSONObject(json)
                val type = MarkerType.fromWireValue(o.getString("type")) ?: return null
                Marker(
                    id = o.getString("id"),
                    lat = o.getDouble("lat"),
                    lon = o.getDouble("lon"),
                    type = type,
                    description = o.optString("description", ""),
                    timestamp = o.getLong("timestamp"),
                    reporterPubKey = o.getString("reporterPubKey"),
                    expiresAt = o.getLong("expiresAt"),
                )
            } catch (e: Exception) {
                null
            }
        }

        fun newMarker(
            lat: Double,
            lon: Double,
            type: MarkerType,
            description: String,
            reporterPubKey: String,
        ): Marker {
            val now = System.currentTimeMillis()
            return Marker(
                lat = lat,
                lon = lon,
                type = type,
                description = description,
                timestamp = now,
                reporterPubKey = reporterPubKey,
                expiresAt = now + type.defaultExpiryMinutes * 60_000L,
            )
        }
    }
}
