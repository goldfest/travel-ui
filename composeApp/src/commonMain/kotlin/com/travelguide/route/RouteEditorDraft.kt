package com.travelguide.route

import com.travelguide.domain.models.TransportMode
import kotlinx.serialization.Serializable

@Serializable
data class RouteEditorDraftPayload(
    val key: String,
    val mode: RouteEditorMode,
    val routeId: Int? = null,
    val selectedCityId: Int? = null,
    val selectedCityName: String = "",
    val routeName: String = "",
    val routeDescription: String = "",
    val selectedTransport: TransportMode = TransportMode.WALK,
    val searchQuery: String = "",
    val days: List<EditableRouteDayUi> = listOf(EditableRouteDayUi(dayNumber = 1)),
    val selectedDayNumber: Int = 1
)

internal fun RouteEditorUiState.toDraftPayload(key: String): RouteEditorDraftPayload =
    RouteEditorDraftPayload(
        key = key,
        mode = mode,
        routeId = routeId,
        selectedCityId = selectedCityId,
        selectedCityName = selectedCityName,
        routeName = routeName,
        routeDescription = routeDescription,
        selectedTransport = selectedTransport,
        searchQuery = searchQuery,
        days = days.ifEmpty { listOf(EditableRouteDayUi(dayNumber = 1)) },
        selectedDayNumber = selectedDayNumber
    )

internal fun RouteEditorUiState.applyDraft(
    draft: RouteEditorDraftPayload,
    availablePois: List<com.travelguide.domain.models.POI>
): RouteEditorUiState {
    val restoredDays = draft.days.ifEmpty { listOf(EditableRouteDayUi(dayNumber = 1)) }
    val safeSelectedDay = restoredDays.firstOrNull { it.dayNumber == draft.selectedDayNumber }?.dayNumber
        ?: restoredDays.first().dayNumber

    return copy(
        routeId = draft.routeId ?: routeId,
        selectedCityId = draft.selectedCityId ?: selectedCityId,
        selectedCityName = draft.selectedCityName.ifBlank { selectedCityName },
        routeName = draft.routeName,
        routeDescription = draft.routeDescription,
        selectedTransport = draft.selectedTransport,
        searchQuery = draft.searchQuery,
        filteredPois = filterDraftPois(availablePois, draft.searchQuery),
        days = restoredDays,
        selectedDayNumber = safeSelectedDay
    )
}

private fun filterDraftPois(source: List<com.travelguide.domain.models.POI>, query: String): List<com.travelguide.domain.models.POI> {
    if (query.isBlank()) return source
    return source.filter { poi ->
        poi.name.contains(query, ignoreCase = true) ||
            (poi.address?.contains(query, ignoreCase = true) == true)
    }
}
