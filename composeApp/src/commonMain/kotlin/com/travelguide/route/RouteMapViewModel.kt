package com.travelguide.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.core.toUserMessage
import com.travelguide.domain.models.RouteMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

data class RouteMapUiState(
    val isLoading: Boolean = false,
    val routeMap: RouteMap? = null,
    val selectedDayNumber: Int? = null,
    val selectedPointId: Int? = null,
    val selectedSegmentPointIds: Set<Int> = emptySet(),
    val errorMessage: String? = null
) {
    val isSegmentSelectionReady: Boolean get() = selectedSegmentPointIds.size >= 2
}

class RouteMapViewModel(
    private val repository: RouteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RouteMapUiState())
    val state: StateFlow<RouteMapUiState> = _state.asStateFlow()

    fun loadRouteMap(routeId: Int) {
        viewModelScope.launch {
            _state.value = RouteMapUiState(isLoading = true)

            runCatching {
                repository.getRouteMap(routeId)
            }.onSuccess { map ->
                val firstDay = map.days.firstOrNull()
                val firstPoint = firstDay?.points?.firstOrNull()

                _state.value = RouteMapUiState(
                    isLoading = false,
                    routeMap = map,
                    selectedDayNumber = firstDay?.dayNumber,
                    selectedPointId = firstPoint?.routePointId,
                    selectedSegmentPointIds = firstPoint?.routePointId?.let { setOf(it) }.orEmpty()
                )
            }.onFailure { e ->
                _state.value = RouteMapUiState(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Не удалось загрузить карту маршрута")
                )
            }
        }
    }


    fun selectDay(dayNumber: Int) {
        val points = _state.value.routeMap?.days
            ?.firstOrNull { it.dayNumber == dayNumber }
            ?.points
            .orEmpty()
        val firstPointId = points.firstOrNull()?.routePointId

        _state.value = _state.value.copy(
            selectedDayNumber = dayNumber,
            selectedPointId = firstPointId,
            selectedSegmentPointIds = firstPointId?.let { setOf(it) }.orEmpty()
        )
    }

    fun selectPoint(pointId: Int) {
        val current = _state.value
        val day = current.routeMap?.days
            ?.firstOrNull { it.dayNumber == current.selectedDayNumber }
            ?: run {
                _state.value = current.copy(selectedPointId = pointId, selectedSegmentPointIds = setOf(pointId))
                return
            }

        val sortedPoints = day.points.sortedBy { it.orderIndex }
        val nextSegmentSelection = buildNextSegmentSelection(
            points = sortedPoints.map { it.routePointId },
            currentSelection = current.selectedSegmentPointIds,
            clickedPointId = pointId
        )

        _state.value = current.copy(
            selectedPointId = pointId,
            selectedSegmentPointIds = nextSegmentSelection
        )
    }
}

private fun buildNextSegmentSelection(
    points: List<Int>,
    currentSelection: Set<Int>,
    clickedPointId: Int
): Set<Int> {
    val clickedIndex = points.indexOf(clickedPointId)
    if (clickedIndex < 0) return setOf(clickedPointId)

    val selectedIndices = currentSelection
        .mapNotNull { pointId -> points.indexOf(pointId).takeIf { it >= 0 } }
        .sorted()

    if (selectedIndices.isEmpty()) return setOf(clickedPointId)

    if (selectedIndices.size == 1) {
        val onlyIndex = selectedIndices.first()
        return if (abs(clickedIndex - onlyIndex) == 1) {
            points.subList(minOf(clickedIndex, onlyIndex), maxOf(clickedIndex, onlyIndex) + 1).toSet()
        } else {
            setOf(clickedPointId)
        }
    }

    val firstIndex = selectedIndices.first()
    val lastIndex = selectedIndices.last()

    return when {
        clickedIndex == firstIndex - 1 -> points.subList(clickedIndex, lastIndex + 1).toSet()
        clickedIndex == lastIndex + 1 -> points.subList(firstIndex, clickedIndex + 1).toSet()
        clickedIndex in firstIndex..lastIndex -> setOf(clickedPointId)
        else -> setOf(clickedPointId)
    }
}
