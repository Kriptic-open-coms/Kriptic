package com.kriptic.app.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kriptic.app.ui.theme.Body
import com.kriptic.app.ui.theme.Caption
import com.kriptic.app.ui.theme.DesignTokens
import com.kriptic.app.ui.theme.Heading
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/**
 * Maps tab per the mockups: full-bleed map, top chip row acting as a
 * marker-type legend/filter, floating "Drop a pin" action, and a
 * bottom-sheet pin-type + description flow on tap.
 *
 * MAP DATA: picks a real basemap when the device has connectivity —
 * OpenFreeMap's free public vector-tile service (real global OSM data,
 * so real Delhi NCR streets/places, not a placeholder) — and falls back
 * to the bundled offline bridge only when there's no connection. See
 * map/MapStyleProvider.kt for why, and map-data/README.md for the state
 * of the offline-only path specifically (still synthetic test data —
 * OpenFreeMap needs connectivity, so it doesn't help with the true
 * zero-connectivity protest case that path exists for).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    markers: List<Marker>,
    onDropPin: (lat: Double, lon: Double, type: MarkerType, description: String) -> Unit,
) {
    var pendingPin by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var selectedType by remember { mutableStateOf<MarkerType?>(null) }
    var description by remember { mutableStateOf("") }
    var mapLoadFailed by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val usingOnlineBasemap = remember { MapStyleProvider.isOnline(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!mapLoadFailed) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val mapView = MapView(ctx)
                    try {
                        PmtilesLocalTileServer.start(ctx)
                    } catch (e: Exception) {
                        android.util.Log.e("MapScreen", "PmtilesLocalTileServer failed to start", e)
                        // Non-fatal: fall through and force the offline style, which
                        // will just render an empty basemap rather than crash if the
                        // local server truly isn't up.
                    }
                    val styleUri = if (usingOnlineBasemap) MapStyleProvider.ONLINE_STYLE_URL else MapStyleProvider.OFFLINE_STYLE_URI
                    try {
                        mapView.apply {
                            onCreate(null)
                            getMapAsync { map ->
                                try {
                                    // Delhi NCR centroid — see map-data/README.md for the bbox this matches.
                                    map.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                                        .target(org.maplibre.android.geometry.LatLng(28.6139, 77.2090))
                                        .zoom(10.0)
                                        .build()
                                    map.setStyle(Style.Builder().fromUri(styleUri)) { style ->
                                        try {
                                            MarkerLayerRenderer.render(style, markers)
                                        } catch (e: Exception) {
                                            android.util.Log.e("MapScreen", "MarkerLayerRenderer.render failed", e)
                                        }
                                        map.addOnMapClickListener { point ->
                                            try {
                                                pendingPin = point.latitude to point.longitude
                                            } catch (e: Exception) {
                                                android.util.Log.e("MapScreen", "onMapClick handling failed", e)
                                            }
                                            true
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("MapScreen", "Map style/camera setup failed", e)
                                    mapLoadFailed = true
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MapScreen", "MapView setup failed", e)
                        mapLoadFailed = true
                    }
                    mapView
                },
                update = { mapView ->
                    mapView.getMapAsync { map ->
                        map.style?.let { MarkerLayerRenderer.render(it, markers) }
                    }
                },
            )

            if (!usingOnlineBasemap) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(DesignTokens.Spacing.md.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            MaterialTheme.shapes.small,
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text("Offline sample data — not real map coverage", style = Caption)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Map failed to load. Check Logcat for \"PmtilesLocalTileServer\" —\n" +
                            "see map/MapScreen.kt and map-data/README.md.",
                    style = Body,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }

        // Legend / filter chip row — same shared component as Knowledge tab.
        LazyRow(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(top = DesignTokens.Spacing.md.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = DesignTokens.Spacing.md.dp),
        ) {
            items(MarkerType.entries) { type ->
                AssistChip(onClick = {}, label = { Text(type.label, style = Caption) })
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                // Center of current view is a reasonable default drop point
                // until the user has actually panned/tapped — real
                // implementation should use the map's current camera target.
                pendingPin = pendingPin ?: (28.6139 to 77.2090) // Delhi NCR centroid fallback
            },
            icon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
            text = { Text("Drop a pin") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(DesignTokens.Spacing.lg.dp),
        )
    }

    pendingPin?.let { (lat, lon) ->
        ModalBottomSheet(onDismissRequest = { pendingPin = null; selectedType = null; description = "" }) {
            Column(modifier = Modifier.padding(DesignTokens.Spacing.lg.dp)) {
                Text("Choose your pin type", style = Heading)
                Spacer(Modifier.height(DesignTokens.Spacing.md.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MarkerType.entries) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.label) },
                        )
                    }
                }
                Spacer(Modifier.height(DesignTokens.Spacing.md.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Describe your pin") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(DesignTokens.Spacing.md.dp))
                Button(
                    onClick = {
                        val type = selectedType ?: return@Button
                        onDropPin(lat, lon, type, description)
                        pendingPin = null
                        selectedType = null
                        description = ""
                    },
                    enabled = selectedType != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Confirm")
                }
                Spacer(Modifier.height(DesignTokens.Spacing.md.dp))
            }
        }
    }
}