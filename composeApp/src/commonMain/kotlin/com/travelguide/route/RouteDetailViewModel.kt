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
            runCatching {
                val route = repository.getRouteById(routeId)
                val isOffline = repository.isRouteOffline(routeId)
                RouteDetailUiState(
                    isLoading = false,
                    route = route,
                    isOfflineAvailable = isOffline,
                    isOfflineMode = false,
                    errorMessage = null
                )
            }.onSuccess {
                _state.value = it
            }.onFailure { e ->
                _state.value = RouteDetailUiState(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Не удалось загрузить маршрут")
                )
            }
        }
    }

    fun loadOfflineRoute(routeId: Int) {
        viewModelScope.launch {
            _state.value = RouteDetailUiState(isLoading = true)
            runCatching {
                RouteDetailUiState(
                    isLoading = false,
                    route = repository.getOfflineRouteById(routeId),
                    isOfflineAvailable = true,
                    isOfflineMode = true,
                    errorMessage = null
                )
            }.onSuccess {
                _state.value = it
            }.onFailure { e ->
                _state.value = RouteDetailUiState(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Не удалось загрузить оффлайн-маршрут")
                )
            }
        }
    }

    fun optimize(routeId: Int, form: RouteOptimizationForm) {
        viewModelScope.launch {
            val current = _state.value.route ?: return@launch
            if (_state.value.isOfflineMode) return@launch

            _state.value = _state.value.copy(
                isLoading = false,
                isOptimizing = true,
                route = current,
                errorMessage = null
            )

            runCatching { repository.optimizeRoute(routeId, form.toDto()) }
                .onSuccess { route ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isOptimizing = false,
                        route = route,
                        errorMessage = null,
                        optimizationMessage = "Маршрут оптимизирован",
                        optimizationMessageId = System.currentTimeMillis()
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isOptimizing = false,
                        errorMessage = e.toUserMessage("Не удалось оптимизировать маршрут")
                    )
                }
        }
    }
}
