package com.travelguide.ui.screens.route

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.travelguide.domain.models.RouteMapDay
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView

@Composable
fun YandexRouteMapView(
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
            val map = mv.mapWindow.map
            val objects = map.mapObjects
            val currentDay = day ?: return@AndroidView

            val pointsKey = currentDay.points.joinToString("|") {
                "${it.routePointId}:${it.orderIndex}:${it.latitude}:${it.longitude}"
            }
            val polylineKey = currentDay.polyline?.coordinates
                ?.joinToString("|") { "${it.latitude}:${it.longitude}" }
                .orEmpty()

            val renderKey = "${currentDay.dayNumber}_${selectedPointId}_${pointsKey}_${polylineKey}"

            if (lastRenderedKey != renderKey) {
                objects.clear()

                val coordinates = currentDay.polyline?.coordinates.orEmpty()
                if (coordinates.size >= 2) {
                    val polylineObject = objects.addPolyline(
                        Polyline(
                            coordinates.map {
                                com.yandex.mapkit.geometry.Point(it.latitude, it.longitude)
                            }
                        )
                    )
                    polylineObject.apply {
                        strokeWidth = 6f
                        setStrokeColor(routeStrokeColor())
                        outlineWidth = 2f
                        outlineColor = routeOutlineColor()
                    }
                }

                val tapListener = MapObjectTapListener { mapObject, _ ->
                    val userData = mapObject.userData as? Int
                    if (userData != null) {
                        onPointClick(userData)
                        true
                    } else {
                        false
                    }
                }

                currentDay.points.forEach { point ->
                    val placemark = addStyledPlacemark(
                        context = context,
                        point = point,
                        selected = point.routePointId == selectedPointId,
                        createPlacemark = { objects.addPlacemark() }
                    )
                    placemark.userData = point.routePointId
                    placemark.addTapListener(tapListener)
                }

                val selectedPoint = currentDay.points.firstOrNull { it.routePointId == selectedPointId }

                if (selectedPoint != null) {
                    focusOnPoint(map, selectedPoint)
                } else {
                    fitRouteToBounds(map, currentDay)
                }

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
    val mapView = remember { MapView(context) }

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