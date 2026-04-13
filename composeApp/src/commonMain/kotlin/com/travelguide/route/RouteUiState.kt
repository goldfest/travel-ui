package com.travelguide.route

import com.travelguide.domain.models.POI
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.TransportMode

enum class RouteListFilter {
    ACTIVE,
    ARCHIVED,
    OFFLINE
}

data class RouteListUiState(
    val isLoading: Boolean = false,
    val routes: List<Route> = emptyList(),
    val filter: RouteListFilter = RouteListFilter.ACTIVE,
    val deletingRouteId: Int? = null,
    val errorMessage: String? = null,
    val syncStatusMessage: String? = null,
    val syncStatusMessageId: Long = 0L,
    val hasPendingSync: Boolean = false,
    val routeIdsWithDrafts: Set<Int> = emptySet(),
    val showApplyDraftsDialog: Boolean = false,
    val isApplyingDrafts: Boolean = false,
    val syncingRouteIds: Set<Int> = emptySet()
)

data class RouteDetailUiState(
    val isLoading: Boolean = false,
    val route: Route? = null,
    val isOfflineAvailable: Boolean = false,
    val errorMessage: String? = null,
    val isOfflineMode: Boolean = false
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
