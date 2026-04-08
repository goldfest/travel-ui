package com.travelguide.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RouteDetailViewModel(
    private val repository: RouteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RouteDetailUiState())
    val state: StateFlow<RouteDetailUiState> = _state.asStateFlow()

    fun loadRoute(routeId: Int) {
        viewModelScope.launch {
            _state.value = RouteDetailUiState(isLoading = true)

            runCatching {
                repository.getRouteById(routeId)
            }.onSuccess { route ->
                _state.value = RouteDetailUiState(
                    isLoading = false,
                    route = route,
                    errorMessage = null
                )
            }.onFailure { e ->
                _state.value = RouteDetailUiState(
                    isLoading = false,
                    route = null,
                    errorMessage = e.message ?: "Не удалось загрузить маршрут"
                )
            }
        }
    }

    fun optimize(routeId: Int) {
        viewModelScope.launch {
            val current = _state.value.route ?: return@launch
            _state.value = _state.value.copy(isLoading = true, route = current)

            runCatching {
                repository.optimizeRoute(routeId)
            }.onSuccess { route ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    route = route,
                    errorMessage = null
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Не удалось оптимизировать маршрут"
                )
            }
        }
    }

    fun reorderDay(routeId: Int, dayId: Int, orderedPointIds: List<Int>) {
        viewModelScope.launch {
            runCatching {
                repository.reorderDayPoints(routeId, dayId, orderedPointIds)
            }.onSuccess { route ->
                _state.value = _state.value.copy(route = route, errorMessage = null)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    errorMessage = e.message ?: "Не удалось изменить порядок точек"
                )
            }
        }
    }
}
