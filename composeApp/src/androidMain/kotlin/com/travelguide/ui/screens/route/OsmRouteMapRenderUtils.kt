package com.travelguide.ui.screens.route

import android.content.Context
import androidx.core.content.ContextCompat
import com.travelguide.R
import com.travelguide.domain.models.RouteMapDay
import com.travelguide.domain.models.RouteMapPoint
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.Locale

fun RouteMapDay.toGeoPoints(): List<GeoPoint> =
    points.map { GeoPoint(it.latitude, it.longitude) }

fun fitRouteToBounds(mapView: MapView, day: RouteMapDay) {
    val points = day.toGeoPoints()
    if (points.isEmpty()) return

    if (points.size == 1) {
        mapView.controller.setZoom(15.5)
        mapView.controller.setCenter(points.first())
        return
    }

    val minLat = points.minOf { it.latitude }
    val maxLat = points.maxOf { it.latitude }
    val minLon = points.minOf { it.longitude }
    val maxLon = points.maxOf { it.longitude }

    mapView.zoomToBoundingBox(BoundingBox(maxLat, maxLon, minLat, minLon), true, 96)
}

fun focusOnPoint(mapView: MapView, point: RouteMapPoint) {
    mapView.controller.animateTo(GeoPoint(point.latitude, point.longitude))
    mapView.controller.setZoom(16.0)
}

fun buildRoutePolyline(day: RouteMapDay): Polyline? {
    val coordinates = day.polyline?.coordinates.orEmpty()
    if (coordinates.size < 2) return null

    return Polyline().apply {
        setPoints(coordinates.map { GeoPoint(it.latitude, it.longitude) })
        outlinePaint.color = routeStrokeColor()
        outlinePaint.strokeWidth = 10f
    }
}

fun buildMarker(
    context: Context,
    mapView: MapView,
    point: RouteMapPoint,
    selected: Boolean,
    onTap: (Int) -> Unit
): Marker {
    val markerType = point.markerType.uppercase(Locale.ROOT)
    val iconRes = when {
        selected -> R.drawable.ic_route_selected
        markerType == "START" -> R.drawable.ic_route_start
        markerType == "END" -> R.drawable.ic_route_end
        else -> R.drawable.ic_route_waypoint
    }

    return Marker(mapView).apply {
        position = GeoPoint(point.latitude, point.longitude)
        icon = ContextCompat.getDrawable(context, iconRes)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title = point.poiName ?: "Точка"
        subDescription = point.poiAddress
        setOnMarkerClickListener { _, _ ->
            onTap(point.routePointId)
            true
        }
    }
}

fun routeStrokeColor(): Int = android.graphics.Color.parseColor("#1565C0")
