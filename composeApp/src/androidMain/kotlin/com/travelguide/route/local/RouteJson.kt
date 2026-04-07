package com.travelguide.route.local

import com.travelguide.domain.models.POI
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteMap
import com.travelguide.route.offline.PendingRouteSyncOperation
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

object RouteJson {
    val format = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encodeRoute(route: Route): String = format.encodeToString(Route.serializer(), route)
    fun decodeRoute(value: String): Route = format.decodeFromString(Route.serializer(), value)

    fun encodeRouteMap(routeMap: RouteMap): String = format.encodeToString(RouteMap.serializer(), routeMap)
    fun decodeRouteMap(value: String): RouteMap = format.decodeFromString(RouteMap.serializer(), value)

    fun encodePois(pois: List<POI>): String =
        format.encodeToString(ListSerializer(POI.serializer()), pois)

    fun decodePois(value: String): List<POI> =
        format.decodeFromString(ListSerializer(POI.serializer()), value)

    fun decodePendingOperation(value: String): PendingRouteSyncOperation =
        format.decodeFromString(PendingRouteSyncOperation.serializer(), value)
}
