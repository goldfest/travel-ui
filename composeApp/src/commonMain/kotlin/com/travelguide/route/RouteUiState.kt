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

data class CreateRouteUiState(
    val isLoading: Boolean = false,
    val routeName: String = "",
    val routeDescription: String = "",
    val selectedTransport: TransportMode = TransportMode.WALK,
    val availablePois: List<POI> = emptyList(),
    val filteredPois: List<POI> = emptyList(),
    val selectedPois: List<POI> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val createdRouteId: Int? = null
)