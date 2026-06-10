package com.travelguide.ui.screens.route

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import com.travelguide.R
import com.travelguide.domain.models.LatLng
import com.travelguide.domain.models.RouteMapDay
import com.travelguide.domain.models.RouteMapPoint
import com.travelguide.domain.models.RouteSegment
import com.travelguide.theme.ExplorerMapLine
import com.travelguide.theme.ExplorerMapLineAccent
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import java.util.Locale
import kotlin.math.roundToInt

fun RouteMapDay.toGeoPoints(selectedSegmentPointIds: Set<Int> = emptySet()): List<GeoPoint> {
    val selectedPoints = points
        .sortedBy { it.orderIndex }
        .filter { selectedSegmentPointIds.size >= 2 && it.routePointId in selectedSegmentPointIds }

    return (selectedPoints.takeIf { it.size >= 2 } ?: points)
        .map { GeoPoint(it.latitude, it.longitude) }
}

fun fitRouteToBounds(
    mapView: MapView,
    day: RouteMapDay,
    selectedSegmentPointIds: Set<Int> = emptySet()
) {
    val points = day.toGeoPoints(selectedSegmentPointIds)
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

    mapView.zoomToBoundingBox(BoundingBox(maxLat, maxLon, minLat, minLon), true, 120)
}

fun focusOnPoint(mapView: MapView, point: RouteMapPoint) {
    mapView.controller.animateTo(GeoPoint(point.latitude, point.longitude))
    mapView.controller.setZoom(16.4)
}

fun buildRoutePolyline(
    day: RouteMapDay,
    selectedSegmentPointIds: Set<Int> = emptySet()
): List<Overlay> {
    val coordinates = if (selectedSegmentPointIds.size >= 2) {
        buildSelectedSegmentCoordinates(day, selectedSegmentPointIds)
    } else {
        day.polyline?.coordinates.orEmpty()
    }

    if (coordinates.size < 2) return emptyList()

    val geoPoints = coordinates.map { GeoPoint(it.latitude, it.longitude) }

    val base = Polyline().apply {
        setPoints(geoPoints)
        outlinePaint.color = routeStrokeShadowColor()
        outlinePaint.strokeWidth = 16f
        outlinePaint.isAntiAlias = true
    }
    val accent = Polyline().apply {
        setPoints(geoPoints)
        outlinePaint.color = routeStrokeColor()
        outlinePaint.strokeWidth = 9f
        outlinePaint.isAntiAlias = true
    }

    return listOf(base, accent)
}

private fun buildSelectedSegmentCoordinates(
    day: RouteMapDay,
    selectedSegmentPointIds: Set<Int>
): List<LatLng> {
    val selectedPoints = day.points
        .sortedBy { it.orderIndex }
        .filter { it.routePointId in selectedSegmentPointIds }

    if (selectedPoints.size < 2) return emptyList()

    val segmentsByPair = day.segments.associateBy { it.fromRoutePointId to it.toRoutePointId }
    val result = mutableListOf<LatLng>()

    selectedPoints.zipWithNext().forEach { (from, to) ->
        val segment = segmentsByPair[from.routePointId to to.routePointId]
            ?: segmentsByPair[to.routePointId to from.routePointId]

        val segmentCoordinates = segment?.safeCoordinates(from, to).orEmpty()
            .ifEmpty {
                listOf(
                    LatLng(from.latitude, from.longitude),
                    LatLng(to.latitude, to.longitude)
                )
            }

        segmentCoordinates.forEach { coordinate ->
            if (result.lastOrNull() != coordinate) {
                result += coordinate
            }
        }
    }

    return result
}

private fun RouteSegment.safeCoordinates(
    from: RouteMapPoint,
    to: RouteMapPoint
): List<LatLng> {
    val coordinates = polyline?.coordinates.orEmpty()
    if (coordinates.isNotEmpty()) return coordinates

    return listOf(
        LatLng(from.latitude, from.longitude),
        LatLng(to.latitude, to.longitude)
    )
}

fun buildMarker(
    context: Context,
    mapView: MapView,
    point: RouteMapPoint,
    selected: Boolean,
    onTap: (Int) -> Unit
): Marker {
    val markerType = point.markerType.uppercase(Locale.ROOT)
    val fallbackIconRes = when {
        selected -> R.drawable.ic_route_selected
        markerType == "START" -> R.drawable.ic_route_start
        markerType == "END" -> R.drawable.ic_route_end
        else -> R.drawable.ic_route_waypoint
    }

    return Marker(mapView).apply {
        position = GeoPoint(point.latitude, point.longitude)
        icon = numberedMarkerDrawable(
            context = context,
            fallbackRes = fallbackIconRes,
            number = point.orderIndex,
            selected = selected,
            markerType = markerType
        ) ?: ContextCompat.getDrawable(context, fallbackIconRes)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title = buildString {
            append(point.orderIndex)
            append(". ")
            append(point.poiName ?: "Точка")
        }
        subDescription = point.poiAddress
        setOnMarkerClickListener { _, _ ->
            onTap(point.routePointId)
            true
        }
    }
}

fun applyMapTheme(mapView: MapView, darkTheme: Boolean) {
    mapView.overlayManager.tilesOverlay.setColorFilter(null)
}

fun routeStrokeColor(): Int = ExplorerMapLine.toArgb()
fun routeStrokeShadowColor(): Int = ExplorerMapLineAccent.copy(alpha = 0.55f).toArgb()

private fun numberedMarkerDrawable(
    context: Context,
    fallbackRes: Int,
    number: Int,
    selected: Boolean,
    markerType: String
) = try {
    val sizePx = 104
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        alpha = if (selected) 255 else 210
        style = Paint.Style.FILL
    }
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = when {
            selected -> android.graphics.Color.parseColor("#F2994A")
            markerType == "START" -> android.graphics.Color.parseColor("#219653")
            markerType == "END" -> android.graphics.Color.parseColor("#1B7F49")
            else -> android.graphics.Color.parseColor("#2D9B63")
        }
        style = Paint.Style.FILL
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 34f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0F2617")
        alpha = 60
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    canvas.drawCircle(sizePx / 2f, sizePx / 2.3f, 30f, haloPaint)
    canvas.drawCircle(sizePx / 2f, sizePx / 2.3f, 24f, fillPaint)
    canvas.drawCircle(sizePx / 2f, sizePx / 2.3f, 24f, strokePaint)

    val tail = floatArrayOf(
        sizePx / 2f, 88f,
        sizePx / 2f - 12f, 56f,
        sizePx / 2f + 12f, 56f
    )
    val tailPath = android.graphics.Path().apply {
        moveTo(tail[0], tail[1])
        lineTo(tail[2], tail[3])
        lineTo(tail[4], tail[5])
        close()
    }
    canvas.drawPath(tailPath, fillPaint)
    canvas.drawPath(tailPath, strokePaint)

    val textY = sizePx / 2.3f - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(number.toString(), sizePx / 2f, textY, textPaint)

    android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
} catch (_: Exception) {
    ContextCompat.getDrawable(context, fallbackRes)
}

private fun androidx.compose.ui.graphics.Color.toArgb(): Int =
    android.graphics.Color.argb(
        (alpha * 255).roundToInt(),
        (red * 255).roundToInt(),
        (green * 255).roundToInt(),
        (blue * 255).roundToInt()
    )
