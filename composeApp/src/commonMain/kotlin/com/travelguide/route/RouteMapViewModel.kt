package com.travelguide.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.domain.models.RouteMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.travelguide.core.toUserMessage
data class RouteMapUiState(
    val isLoading: Boolean = false,
    val routeMap: RouteMap? = null,
    val selectedDayNumber: Int? = null,
    val selectedPointId: Int? = null,
    val errorMessage: String? = null
)

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
                    selectedPointId = firstPoint?.routePointId
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

        _state.value = _state.value.copy(
            selectedDayNumber = dayNumber,
            selectedPointId = points.firstOrNull()?.routePointId
        )
    }

    fun selectPoint(pointId: Int) {
        _state.value = _state.value.copy(selectedPointId = pointId)
    }
}