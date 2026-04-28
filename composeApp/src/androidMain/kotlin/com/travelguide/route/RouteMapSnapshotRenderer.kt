package com.travelguide.route

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.travelguide.domain.models.RouteMapDay
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import com.travelguide.ui.map.TravelMapTileSources

class RouteMapSnapshotRenderer(
    private val context: Context
) {
    fun render(day: RouteMapDay, width: Int = 1200, height: Int = 800): Bitmap {
        Configuration.getInstance().userAgentValue = context.packageName
        val mapView = MapView(context).apply {
            setTileSource(TravelMapTileSources.CartoPositron)
            setMultiTouchControls(false)
            layout(0, 0, width, height)
        }

        buildRoutePolyline(day)?.let(mapView.overlays::add)
        day.points.forEach { point ->
            mapView.overlays.add(
                Marker(mapView).apply {
                    position = GeoPoint(point.latitude, point.longitude)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = point.poiName ?: "Точка"
                    subDescription = point.poiAddress
                }
            )
        }
        fitRouteToBounds(mapView, day)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mapView.draw(Canvas(bitmap))
        return bitmap
    }

    private fun buildRoutePolyline(day: RouteMapDay): Polyline? {
        val coordinates = day.polyline?.coordinates.orEmpty()
        if (coordinates.size < 2) return null

        return Polyline().apply {
            setPoints(coordinates.map { GeoPoint(it.latitude, it.longitude) })
            outlinePaint.strokeWidth = 10f
        }
    }

    private fun fitRouteToBounds(mapView: MapView, day: RouteMapDay) {
        val points = day.points.map { GeoPoint(it.latitude, it.longitude) }
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
}
