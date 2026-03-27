package com.travelguide.ui.screens.route

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.travelguide.domain.models.RouteMapDay
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView

@Composable
fun YandexRouteMapView(
    day: RouteMapDay?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = rememberMapViewWithLifecycle(context, lifecycleOwner)

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { mv ->
            val map = mv.mapWindow.map
            val objects = map.mapObjects
            objects.clear()

            val currentDay = day ?: return@AndroidView

            val coordinates = currentDay.polyline?.coordinates.orEmpty()
            if (coordinates.size >= 2) {
                val polyline = Polyline(
                    coordinates.map { Point(it.latitude, it.longitude) }
                )
                objects.addPolyline(polyline).apply {
                    strokeWidth = 5f
                }
            }

            currentDay.points.forEach { point ->
                val placemark = objects.addPlacemark(Point(point.latitude, point.longitude))
                placemark.setText((point.orderIndex).toString())
            }

            val first = currentDay.points.firstOrNull()
            if (first != null) {
                map.move(
                    CameraPosition(
                        Point(first.latitude, first.longitude),
                        13f,
                        0f,
                        0f
                    )
                )
            }
        }
    )
}

@Composable
private fun rememberMapViewWithLifecycle(
    context: Context,
    lifecycleOwner: LifecycleOwner
): MapView {
    val mapView = MapView(context)

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                MapKitFactory.getInstance().onStart()
                mapView.onStart()
            }

            override fun onStop(owner: LifecycleOwner) {
                mapView.onStop()
                MapKitFactory.getInstance().onStop()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return mapView
}