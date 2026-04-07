package com.travelguide.route

import com.travelguide.domain.models.*
import com.travelguide.network.dto.route.*

fun RouteMapResponseDto.toDomain(): RouteMap =
    RouteMap(
        routeId = routeId.toInt(),
        routeName = routeName,
        description = description,
        transportMode = transportMode,
        totalDistanceKm = totalDistanceKm,
        totalDurationMin = totalDurationMin,
        viewport = viewport?.let {
            MapViewport(
                minLat = it.minLat,
                minLng = it.minLng,
                maxLat = it.maxLat,
                maxLng = it.maxLng,
                centerLat = it.centerLat,
                centerLng = it.centerLng
            )
        },
        days = days.map { day ->
            RouteMapDay(
                routeDayId = day.routeDayId.toInt(),
                dayNumber = day.dayNumber,
                polyline = day.polyline?.let { poly ->
                    RoutePolyline(
                        source = poly.source,
                        coordinates = poly.coordinates.map { LatLng(it.latitude, it.longitude) }
                    )
                },
                points = day.points.map { point ->
                    RouteMapPoint(
                        routePointId = point.routePointId.toInt(),
                        poiId = point.poiId.toInt(),
                        orderIndex = point.orderIndex,
                        poiName = point.poiName,
                        poiAddress = point.poiAddress,
                        poiType = point.poiType,
                        latitude = point.latitude,
                        longitude = point.longitude,
                        markerType = point.markerType,
                        estimatedVisitMinutes = point.estimatedVisitMinutes
                    )
                },
                segments = day.segments.map { seg ->
                    RouteSegment(
                        fromRoutePointId = seg.fromRoutePointId.toInt(),
                        toRoutePointId = seg.toRoutePointId.toInt(),
                        distanceKm = seg.distanceKm,
                        durationMin = seg.durationMin,
                        transportMode = seg.transportMode,
                        polyline = seg.polyline?.let { poly ->
                            RoutePolyline(
                                source = poly.source,
                                coordinates = poly.coordinates.map { LatLng(it.latitude, it.longitude) }
                            )
                        },
                        provider = seg.provider,
                        status = seg.status
                    )
                }
            )
        }
    )