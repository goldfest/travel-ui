package com.travelguide.route

import com.travelguide.domain.models.POI
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.TransportMode

data class RouteListUiState(
    val isLoading: Boolean = false,
    val routes: List<Route> = emptyList(),
    val showArchived: Boolean = false,
    val errorMessage: String? = null
)

data class RouteDetailUiState(
    val isLoading: Boolean = false,
    val route: Route? = null,
    val errorMessage: String? = null
)

data class EditableRoutePointUi(
    val poi: POI,
    val estimatedVisitMinutes: Int = 60
)

data class EditableRouteDayUi(
    val dayNumber: Int,
    val description: String = "",
    val points: List<EditableRoutePointUi> = emptyList()
)

data class CreateRouteUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val routeName: String = "",
    val routeDescription: String = "",
    val selectedTransport: TransportMode = TransportMode.WALK,
    val availablePois: List<POI> = emptyList(),
    val filteredPois: List<POI> = emptyList(),
    val days: List<EditableRouteDayUi> = listOf(
        EditableRouteDayUi(dayNumber = 1)
    ),
    val selectedDayNumber: Int = 1,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val createdRouteId: Int? = null
)