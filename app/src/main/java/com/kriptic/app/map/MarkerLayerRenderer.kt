package com.kriptic.app.map

import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.maps.Style
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

/**
 * NOTE: written against the documented MapLibre Native Android SDK API
 * (org.maplibre.gl:android-sdk) as of when this was written — verify
 * import paths/method names against whatever SDK version actually resolves
 * at build time, since this couldn't be compiled in the sandbox this was
 * generated in (no Android SDK / Gradle toolchain available there).
 */
object MarkerLayerRenderer {
    private const val SOURCE_ID = "kriptic-markers-source"
    private const val LAYER_ID = "kriptic-markers-layer"

    fun render(style: Style, markers: List<Marker>) {
        val features = markers.map { marker ->
            Feature.fromGeometry(Point.fromLngLat(marker.lon, marker.lat)).apply {
                addStringProperty("type", marker.type.name)
                addStringProperty("description", marker.description)
                addNumberProperty("corroborationCount", marker.corroborationCount)
            }
        }
        val collection = FeatureCollection.fromFeatures(features)

        val existingSource = style.getSourceAs<GeoJsonSource>(SOURCE_ID)
        if (existingSource != null) {
            existingSource.setGeoJson(collection)
            return
        }

        style.addSource(GeoJsonSource(SOURCE_ID, collection))
        style.addLayer(
            CircleLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.circleRadius(8f),
                PropertyFactory.circleColor(
                    Expression.match(
                        Expression.get("type"),
                        Expression.color(MarkerType.GATHER.color.toArgbInt()),
                        Expression.stop(MarkerType.DANGER.name, Expression.color(MarkerType.DANGER.color.toArgbInt())),
                        Expression.stop(MarkerType.SAFE.name, Expression.color(MarkerType.SAFE.color.toArgbInt())),
                        Expression.stop(MarkerType.POLICE.name, Expression.color(MarkerType.POLICE.color.toArgbInt())),
                        Expression.stop(MarkerType.HELP.name, Expression.color(MarkerType.HELP.color.toArgbInt())),
                        Expression.stop(MarkerType.GATHER.name, Expression.color(MarkerType.GATHER.color.toArgbInt()))
                    )
                ),
                PropertyFactory.circleStrokeWidth(1.5f),
                PropertyFactory.circleStrokeColor("#FFFFFF"),
            )
        )
    }

    private fun androidx.compose.ui.graphics.Color.toArgbInt(): Int = android.graphics.Color.argb(
        (alpha * 255).toInt(), (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt()
    )
}
