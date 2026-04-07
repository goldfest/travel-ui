package com.travelguide.ui.screens.route

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.travelguide.domain.models.RouteMapDay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView

@Composable
fun OsmRouteMapView(
    day: RouteMapDay?,
    selectedPointId: Int?,
    modifier: Modifier = Modifier,
    onPointClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = rememberMapViewWithLifecycle(context, lifecycleOwner)

    var lastRenderedKey by remember { mutableStateOf<String?>(null) }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { mv ->
            val currentDay = day ?: return@AndroidView
            val pointsKey = currentDay.points.joinToString("|") {
                "${it.routePointId}:${it.orderIndex}:${it.latitude}:${it.longitude}"
            }
            val polylineKey = currentDay.polyline?.coordinates
                ?.joinToString("|") { "${it.latitude}:${it.longitude}" }
                .orEmpty()
            val renderKey = "${currentDay.dayNumber}_${selectedPointId}_${pointsKey}_${polylineKey}"

            if (lastRenderedKey != renderKey) {
                mv.overlays.clear()
                buildRoutePolyline(currentDay)?.let { mv.overlays.add(it) }

                currentDay.points.forEach { point ->
                    mv.overlays.add(
                        buildMarker(
                            context = context,
                            mapView = mv,
                            point = point,
                            selected = point.routePointId == selectedPointId,
                            onTap = onPointClick
                        )
                    )
                }

                val selectedPoint = currentDay.points.firstOrNull { it.routePointId == selectedPointId }
                if (selectedPoint != null) focusOnPoint(mv, selectedPoint) else fitRouteToBounds(mv, currentDay)
                mv.invalidate()
                lastRenderedKey = renderKey
            }
        }
    )
}

@Composable
private fun rememberMapViewWithLifecycle(
    context: Context,
    lifecycleOwner: LifecycleOwner
): MapView {
    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.0)
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return mapView
}
