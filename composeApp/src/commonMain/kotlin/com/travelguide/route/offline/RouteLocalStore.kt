package com.travelguide.route.offline

import com.travelguide.domain.models.POI
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteMap

interface RouteLocalStore {
    suspend fun getRoutes(archived: Boolean): List<Route>
    suspend fun saveRoutes(routes: List<Route>)
    suspend fun getRoute(routeId: Int): Route?
    suspend fun saveRoute(route: Route)
    suspend fun deleteRoute(routeId: Int)

    suspend fun getRouteMap(routeId: Int): RouteMap?
    suspend fun saveRouteMap(routeMap: RouteMap)
    suspend fun deleteRouteMap(routeId: Int)

    suspend fun getPoisByCity(cityId: Int): List<POI>
    suspend fun savePoisByCity(cityId: Int, pois: List<POI>)
    suspend fun findPoiById(poiId: Int): POI?

    suspend fun enqueue(operation: PendingRouteSyncOperation)
    suspend fun replacePendingCreate(operation: PendingRouteSyncOperation)
}
