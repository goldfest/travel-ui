package com.travelguide.route.offline

import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteDay
import com.travelguide.domain.models.RoutePoint
import com.travelguide.domain.models.RouteStatus
import com.travelguide.domain.models.TransportMode
import com.travelguide.route.EditableRouteDayUi
import com.travelguide.route.EditableRoutePointUi
import kotlinx.serialization.Serializable

@Serializable
enum class RouteSyncOperationType {
    CREATE_ROUTE,
    UPDATE_ROUTE_META,
    ADD_POINT,
    REMOVE_POINT,
    REORDER_DAY,
    OPTIMIZE_ROUTE
}

@Serializable
data class PendingRouteSyncOperation(
    val routeId: Int?,
    val operationType: RouteSyncOperationType,
    val payloadJson: String
)

@Serializable
data class CreateRouteSyncPayload(
    val localRouteId: Int,
    val cityId: Int,
    val name: String,
    val description: String? = null,
    val transportMode: TransportMode,
    val status: RouteStatus = RouteStatus.READY,
    val autoOptimize: Boolean = false,
    val optimizationMode: String? = null,
    val days: List<CreateRouteDaySnapshot> = emptyList()
) {
    companion object {
        fun fromRoute(route: Route): CreateRouteSyncPayload =
            CreateRouteSyncPayload(
                localRouteId = route.id,
                cityId = route.cityId,
                name = route.name,
                description = route.description,
                transportMode = route.transportMode,
                status = route.status,
                autoOptimize = route.isOptimized,
                optimizationMode = route.optimizationMode,
                days = route.days.sortedBy { it.dayNumber }.map { CreateRouteDaySnapshot.fromDay(it) }
            )

        fun fromEditor(
            localRouteId: Int,
            cityId: Int,
            name: String,
            description: String?,
            transportMode: TransportMode,
            status: RouteStatus,
            autoOptimize: Boolean,
            optimizationMode: String?,
            days: List<EditableRouteDayUi>
        ): CreateRouteSyncPayload =
            CreateRouteSyncPayload(
                localRouteId = localRouteId,
                cityId = cityId,
                name = name,
                description = description,
                transportMode = transportMode,
                status = status,
                autoOptimize = autoOptimize,
                optimizationMode = optimizationMode,
                days = days.map { CreateRouteDaySnapshot.fromEditable(it) }
            )
    }
}

@Serializable
data class CreateRouteDaySnapshot(
    val dayNumber: Int,
    val description: String? = null,
    val points: List<CreateRoutePointSnapshot> = emptyList()
) {
    companion object {
        fun fromDay(day: RouteDay): CreateRouteDaySnapshot =
            CreateRouteDaySnapshot(
                dayNumber = day.dayNumber,
                description = day.description,
                points = day.points.sortedBy { it.orderIndex }.map { CreateRoutePointSnapshot.fromRoutePoint(it) }
            )

        fun fromEditable(day: EditableRouteDayUi): CreateRouteDaySnapshot =
            CreateRouteDaySnapshot(
                dayNumber = day.dayNumber,
                description = day.description.takeIf { it.isNotBlank() },
                points = day.points.mapIndexed { index, point ->
                    CreateRoutePointSnapshot.fromEditable(index + 1, point)
                }
            )
    }
}

@Serializable
data class CreateRoutePointSnapshot(
    val poiId: Int,
    val orderIndex: Int,
    val estimatedVisitMinutes: Int = 60
) {
    companion object {
        fun fromRoutePoint(point: RoutePoint): CreateRoutePointSnapshot =
            CreateRoutePointSnapshot(
                poiId = point.poiId,
                orderIndex = point.orderIndex,
                estimatedVisitMinutes = point.estimatedVisitMinutes
            )

        fun fromEditable(orderIndex: Int, point: EditableRoutePointUi): CreateRoutePointSnapshot =
            CreateRoutePointSnapshot(
                poiId = point.poi.id,
                orderIndex = orderIndex,
                estimatedVisitMinutes = point.estimatedVisitMinutes
            )
    }
}

@Serializable
data class UpdateRouteMetaSyncPayload(
    val routeId: Int,
    val name: String,
    val description: String?,
    val transportMode: TransportMode
)

@Serializable
data class AddRoutePointSyncPayload(
    val routeId: Int,
    val poiId: Int,
    val dayNumber: Int,
    val orderIndex: Int? = null
)

@Serializable
data class RemoveRoutePointSyncPayload(
    val routeId: Int,
    val routePointId: Int
)

@Serializable
data class ReorderRouteDaySyncPayload(
    val routeId: Int,
    val dayId: Int,
    val orderedPointIds: List<Int>
)

@Serializable
data class OptimizeRouteSyncPayload(
    val routeId: Int,
    val mode: String = "distance"
)
