package com.travelguide.city

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.travelguide.domain.models.POI
import com.travelguide.ui.screens.route.applyMapTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun CityPoiMapView(
    pois: List<POI>,
    modifier: Modifier = Modifier,
    onPoiClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()

    val mapView = remember {
        createMapView(context)
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onDetach()
        }
    }

    LaunchedEffect(pois, darkTheme) {
        updateMarkersAndViewport(
            mapView = mapView,
            pois = pois,
            onPoiClick = onPoiClick,
            darkTheme = darkTheme
        )
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

private fun createMapView(context: Context): MapView {
    Configuration.getInstance().load(
        context,
        context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
    )

    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(12.0)
        minZoomLevel = 4.0
        maxZoomLevel = 20.0
    }
}

private suspend fun updateMarkersAndViewport(
    mapView: MapView,
    pois: List<POI>,
    onPoiClick: (Int) -> Unit,
    darkTheme: Boolean
) {
    withContext(Dispatchers.Main) {
        mapView.overlays.clear()
        applyMapTheme(mapView, darkTheme)

        val points = mutableListOf<GeoPoint>()

        pois.forEach { poi ->
            val lat = poi.latitude
            val lon = poi.longitude
            if (lat != null && lon != null) {
                val point = GeoPoint(lat, lon)
                points += point

                val marker = Marker(mapView).apply {
                    position = point
                    title = poi.name
                    subDescription = poi.description
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        onPoiClick(poi.id)
                        true
                    }
                }

                mapView.overlays.add(marker)
            }
        }

        mapView.post {
            when {
                points.isEmpty() -> {
                    mapView.controller.setZoom(5.0)
                    mapView.controller.setCenter(GeoPoint(55.751244, 37.618423))
                }

                points.size == 1 -> {
                    mapView.controller.setZoom(15.0)
                    mapView.controller.setCenter(points.first())
                }

                else -> {
                    val boundingBox = BoundingBox.fromGeoPointsSafe(points)
                    mapView.zoomToBoundingBox(boundingBox, true, 80)
                }
            }

            mapView.invalidate()
        }
    }
}
