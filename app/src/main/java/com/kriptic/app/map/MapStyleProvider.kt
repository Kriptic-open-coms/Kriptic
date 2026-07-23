package com.kriptic.app.map

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Kriptic needs to work fully offline during a protest (that's the whole
 * point — see docs/00_BRIEF.md), but most day-to-day use (checking the
 * app before heading out, testing on a dev device) DOES have connectivity.
 * Rather than only ever showing the synthetic 16KB test tileset, this
 * picks a REAL basemap when the device is online:
 *
 * https://tiles.openfreemap.org/styles/liberty — OpenFreeMap's public
 * instance. Free, unlimited, no API key/registration, real global OSM
 * vector tiles (so real Delhi NCR streets, place names, buildings — not
 * a placeholder). MapLibre adds the required "OpenFreeMap © OpenMapTiles
 * / Data from OpenStreetMap" attribution automatically; nothing else to
 * do for compliance. See https://openfreemap.org/.
 *
 * This does NOT replace the offline requirement — OpenFreeMap needs a
 * live connection, so it's useless mid-protest with connectivity cut.
 * The bundled `delhi_ncr_style.json` (currently synthetic sample data,
 * see map-data/README.md for the real-tile pipeline) remains the only
 * path that works with zero connectivity, and is used automatically when
 * the device is offline.
 */
object MapStyleProvider {
    const val ONLINE_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
    const val OFFLINE_STYLE_URI = "asset://map/delhi_ncr_style.json"

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /** Picks online-vs-offline once at map-load time (not live-reactive to connectivity changes mid-session). */
    fun styleUriFor(context: Context): String = if (isOnline(context)) ONLINE_STYLE_URL else OFFLINE_STYLE_URI
}
