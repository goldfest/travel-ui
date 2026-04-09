package com.travelguide.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.core.toUserMessage
import com.travelguide.network.dto.route.RouteOptimizationDayRequestDto
import com.travelguide.network.dto.route.RouteOptimizationRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RouteOptimizationDayForm(
    val routeDayId: Long,
    val routeDate: String,
    val dayStartTime: String,
    val dayEndTime: String
) {
    fun toDto(): RouteOptimizationDayRequestDto = RouteOptimizationDayRequestDto(
        routeDayId = routeDayId,
        routeDate = routeDate.ifBlank { null },
        dayStartTime = dayStartTime.ifBlank { null },
        dayEndTime = dayEndTime.ifBlank { null }
    )
}

data class RouteOptimizationForm(
    val optimizationMode: String = "TIME_WINDOW",
    val daySettings: List<RouteOptimizationDayForm> = emptyList(),
    val visitMinutesByRoutePointId: Map<Long, Int> = emptyMap()
) {
    fun toDto(): RouteOptimizationRequestDto = RouteOptimizationRequestDto(
        optimizationMode = optimizationMode,
        daySettings = daySettings.map { it.toDto() },
        visitMinutesByRoutePointId = visitMinutesByRoutePointId
    )
}

class RouteDetailViewModel(
    private val repository: RouteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RouteDetailUiState())
    val state: StateFlow<RouteDetailUiState> = _state.asStateFlow()

    fun loadRoute(routeId: Int) {
        viewModelScope.launch {
            _state.value = RouteDetailUiState(isLoading = true)
            runCatching { repository.getRouteById(routeId) }
                .onSuccess { route ->
                    _state.value = RouteDetailUiState(isLoading = false, route = route)
                }
                .onFailure { e ->
                    _state.value = RouteDetailUiState(
                        isLoading = false,
                        errorMessage = e.toUserMessage("Не удалось загрузить маршрут")
                    )
                }
        }
    }

    fun optimize(routeId: Int, form: RouteOptimizationForm) {
        viewModelScope.launch {
            val current = _state.value.route ?: return@launch
            _state.value = _state.value.copy(isLoading = true, route = current)
            runCatching { repository.optimizeRoute(routeId, form.toDto()) }
                .onSuccess { route ->
                    _state.value = _state.value.copy(isLoading = false, route = route, errorMessage = null)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = e.toUserMessage("Не удалось оптимизировать маршрут")
                    )
                }
        }
    }

    fun reorderDay(routeId: Int, dayId: Int, orderedPointIds: List<Int>) {
        viewModelScope.launch {
            runCatching { repository.reorderDayPoints(routeId, dayId, orderedPointIds) }
                .onSuccess { route -> _state.value = _state.value.copy(route = route, errorMessage = null) }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        errorMessage = e.toUserMessage("Не удалось изменить порядок точек")
                    )
                }
        }
    }
}
