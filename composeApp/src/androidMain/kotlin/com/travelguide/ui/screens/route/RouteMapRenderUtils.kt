package com.travelguide.ui.screens.route

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import com.travelguide.R
import com.travelguide.domain.models.RouteMapDay
import com.travelguide.domain.models.RouteMapPoint
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider
import java.util.Locale

fun RouteMapDay.toYandexPoints(): List<Point> =
    points.map { Point(it.latitude, it.longitude) }

fun fitRouteToBounds(map: Map, day: RouteMapDay) {
    val points = day.toYandexPoints()
    if (points.isEmpty()) return

    if (points.size == 1) {
        map.move(CameraPosition(points.first(), 15f, 0f, 0f))
        return
    }

    val minLat = points.minOf { it.latitude }
    val maxLat = points.maxOf { it.latitude }
    val minLon = points.minOf { it.longitude }
    val maxLon = points.maxOf { it.longitude }

    val centerLat = (minLat + maxLat) / 2.0
    val centerLon = (minLon + maxLon) / 2.0

    val latSpan = (maxLat - minLat).coerceAtLeast(0.001)
    val lonSpan = (maxLon - minLon).coerceAtLeast(0.001)
    val maxSpan = maxOf(latSpan, lonSpan)

    val zoom = when {
        maxSpan < 0.005 -> 15.5f
        maxSpan < 0.01 -> 14.5f
        maxSpan < 0.02 -> 13.5f
        maxSpan < 0.05 -> 12.5f
        maxSpan < 0.1 -> 11.5f
        maxSpan < 0.2 -> 10.5f
        else -> 9.5f
    }

    map.move(
        CameraPosition(
            Point(centerLat, centerLon),
            zoom,
            0f,
            0f
        )
    )
}

fun focusOnPoint(map: Map, point: RouteMapPoint) {
    map.move(
        CameraPosition(
            Point(point.latitude, point.longitude),
            16f,
            0f,
            0f
        )
    )
}

fun addStyledPlacemark(
    context: Context,
    point: RouteMapPoint,
    selected: Boolean,
    createPlacemark: () -> PlacemarkMapObject
): PlacemarkMapObject {
    val placemark = createPlacemark()

    val markerType = point.markerType?.uppercase(Locale.ROOT)

    val iconRes = when {
        selected -> R.drawable.ic_route_selected
        markerType == "START" -> R.drawable.ic_route_start
        markerType == "END" -> R.drawable.ic_route_end
        else -> R.drawable.ic_route_waypoint
    }

    placemark.geometry = Point(point.latitude, point.longitude)
    placemark.setIcon(
        ImageProvider.fromResource(context, iconRes),
        IconStyle().apply {
            anchor = PointF(0.5f, 1.0f)
            scale = if (selected) 1.1f else 1.0f
        }
    )
    placemark.setText(point.orderIndex.toString())

    return placemark
}

fun routeStrokeColor(): Int = Color.parseColor("#1565C0")
fun routeOutlineColor(): Int = Color.parseColor("#0D47A1")