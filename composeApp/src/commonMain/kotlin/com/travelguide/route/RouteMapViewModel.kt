package com.travelguide.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.domain.models.RouteMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RouteMapUiState(
    val isLoading: Boolean = false,
    val routeMap: RouteMap? = null,
    val selectedDayNumber: Int? = null,
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
                _state.value = RouteMapUiState(
                    isLoading = false,
                    routeMap = map,
                    selectedDayNumber = map.days.firstOrNull()?.dayNumber
                )
            }.onFailure { e ->
                _state.value = RouteMapUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Не удалось загрузить карту маршрута"
                )
            }
        }
    }

    fun selectDay(dayNumber: Int) {
        _state.value = _state.value.copy(selectedDayNumber = dayNumber)
    }
}