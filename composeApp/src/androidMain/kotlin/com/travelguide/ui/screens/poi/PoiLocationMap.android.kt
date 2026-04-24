package com.travelguide.ui.screens.poi

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.travelguide.ui.screens.route.applyMapTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
actual fun PoiLocationMap(
    latitude: Double,
    longitude: Double,
    title: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()

    val mapView = remember {
        createPoiMapView(context)
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onDetach()
        }
    }

    LaunchedEffect(latitude, longitude, title, darkTheme) {
        updatePoiMap(
            mapView = mapView,
            latitude = latitude,
            longitude = longitude,
            title = title,
            darkTheme = darkTheme
        )
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

private fun createPoiMapView(context: Context): MapView {
    Configuration.getInstance().load(
        context,
        context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
    )

    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(15.0)
        minZoomLevel = 4.0
        maxZoomLevel = 20.0
        isTilesScaledToDpi = true
    }
}

private suspend fun updatePoiMap(
    mapView: MapView,
    latitude: Double,
    longitude: Double,
    title: String,
    darkTheme: Boolean
) {
    withContext(Dispatchers.Main) {
        mapView.overlays.clear()
        applyMapTheme(mapView, darkTheme)

        val point = GeoPoint(latitude, longitude)
        val marker = Marker(mapView).apply {
            position = point
            this.title = title
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        mapView.overlays.add(marker)
        mapView.post {
            mapView.controller.setZoom(15.0)
            mapView.controller.setCenter(point)
            mapView.invalidate()
        }
    }
}
