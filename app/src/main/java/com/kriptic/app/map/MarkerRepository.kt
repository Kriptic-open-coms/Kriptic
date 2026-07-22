package com.kriptic.app.map

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Client-side marker aggregation, per docs/01_ARCHITECTURE.md §4:
 * "markers of the same type within a small radius are merged/corroborated,
 * giving basic resistance to a single bad actor spamming fake markers."
 */
class MarkerRepository(context: Context) {

    private val dao = MarkerDatabase.getInstance(context).markerDao()

    companion object {
        /** Same type + same reporter + within this radius = treated as an update, not a new pin. */
        const val CORROBORATION_RADIUS_METERS = 75.0
    }

    fun observeActiveMarkers(): Flow<List<Marker>> = dao.observeActive(System.currentTimeMillis())

    suspend fun sweepExpired() = dao.deleteExpired(System.currentTimeMillis())

    /**
     * Called both when the local user drops a pin AND when a marker arrives
     * over the mesh (see MarkerBroadcastBridge) — same aggregation path
     * either way, so locally-authored and network-received markers behave
     * identically.
     */
    suspend fun ingestAutoCorroborate(marker: Marker) = ingest(marker, dao.getActiveSnapshot(System.currentTimeMillis()))

    suspend fun ingest(marker: Marker, nearbyExisting: List<Marker>) {
        val corroborating = nearbyExisting.firstOrNull {
            it.type == marker.type && distanceMeters(it.lat, it.lon, marker.lat, marker.lon) <= CORROBORATION_RADIUS_METERS
        }
        if (corroborating != null && corroborating.reporterPubKey != marker.reporterPubKey) {
            // Different reporter, same type, same area within the window: bump corroboration
            // and extend expiry slightly, rather than creating a duplicate pin.
            dao.upsert(
                corroborating.copy(
                    corroborationCount = corroborating.corroborationCount + 1,
                    expiresAt = maxOf(corroborating.expiresAt, marker.expiresAt),
                )
            )
        } else {
            dao.upsert(marker)
        }
    }

    /** Haversine distance in meters — good enough at protest/city scale. */
    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /** Panic wipe hook — see security/PanicWipeManager.kt. */
    suspend fun clearAll() = dao.clearAll()
}
