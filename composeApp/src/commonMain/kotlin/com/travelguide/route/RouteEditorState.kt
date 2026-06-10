package com.travelguide.route

import com.travelguide.domain.models.City
import com.travelguide.domain.models.POI
import com.travelguide.domain.models.TransportMode
import kotlinx.serialization.Serializable

@Serializable
enum class RouteEditorMode {
    CREATE,
    EDIT
}

@Serializable
data class EditableRoutePointUi(
    val routePointId: Int? = null,
    val poi: POI,
    val estimatedVisitMinutes: Int = 60
)

@Serializable
data class EditableRouteDayUi(
    val routeDayId: Int? = null,
    val dayNumber: Int,
    val description: String = "",
    val points: List<EditableRoutePointUi> = emptyList()
)

data class RouteEditorUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val mode: RouteEditorMode = RouteEditorMode.CREATE,

    val routeId: Int? = null,

    val availableCities: List<City> = emptyList(),
    val selectedCityId: Int? = null,
    val selectedCityName: String = "",
    val isGraphLoading: Boolean = false,
    val isGraphReady: Boolean = false,
    val isGraphDownloadInProgress: Boolean = false,
    val graphProgressPercent: Int = 0,
    val graphMessage: String? = null,

    val routeName: String = "",
    val routeDescription: String = "",
    val selectedTransport: TransportMode = TransportMode.WALK,

    val availablePois: List<POI> = emptyList(),
    val filteredPois: List<POI> = emptyList(),
    val searchQuery: String = "",

    val days: List<EditableRouteDayUi> = listOf(
        EditableRouteDayUi(dayNumber = 1)
    ),
    val selectedDayNumber: Int = 1,

    val errorMessage: String? = null,
    val syncNoticeMessage: String? = null,
    val hasPendingSync: Boolean = false,
    val snackbarMessage: String? = null,
    val snackbarMessageId: Long = 0L,
    val savedRouteId: Int? = null
)