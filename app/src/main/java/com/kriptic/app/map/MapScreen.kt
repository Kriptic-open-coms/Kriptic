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
 * MAP DATA NOTE: this screen points at
 * `asset://map/delhi_ncr_style.json`, which is expected to reference a
 * bundled `delhi_ncr.pmtiles` file (see map-data/ at the repo root for the
 * build pipeline). That tile file is NOT included in this scaffold — it
 * has to be built from a real OpenStreetMap Delhi NCR extract on a machine
 * that can reach Geofabrik/planetiler, which wasn't reachable from the
 * environment this code was written in. Until that file exists at
 * app/src/main/assets/map/, MapLibre will fail to load the style and this
 * screen falls back to a plain surface so the app doesn't crash.
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

    Box(modifier = Modifier.fillMaxSize()) {
        if (!mapLoadFailed) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    MapView(context).apply {
                        onCreate(null)
                        getMapAsync { map ->
                            try {
                                map.setStyle(Style.Builder().fromUri("asset://map/delhi_ncr_style.json")) { style ->
                                    MarkerLayerRenderer.render(style, markers)
                                    map.addOnMapClickListener { point ->
                                        pendingPin = point.latitude to point.longitude
                                        true
                                    }
                                }
                            } catch (e: Exception) {
                                mapLoadFailed = true
                            }
                        }
                    }
                },
                update = { mapView ->
                    mapView.getMapAsync { map ->
                        map.style?.let { MarkerLayerRenderer.render(it, markers) }
                    }
                },
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Offline map tiles not bundled in this build yet.\nSee map-data/README.md.",
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
