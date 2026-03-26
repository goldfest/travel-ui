package com.travelguide.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.domain.models.POI
import com.travelguide.domain.models.TransportMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateRouteViewModel(
    private val repository: RouteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateRouteUiState())
    val state: StateFlow<CreateRouteUiState> = _state.asStateFlow()

    fun loadPois(cityId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            runCatching {
                repository.getPoisForRouteCreation(cityId)
            }.onSuccess { pois ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    availablePois = pois,
                    filteredPois = filterPois(pois, _state.value.searchQuery),
                    errorMessage = null
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Не удалось загрузить объекты"
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _state.value = _state.value.copy(routeName = value)
    }

    fun onDescriptionChange(value: String) {
        _state.value = _state.value.copy(routeDescription = value)
    }

    fun onTransportChange(value: TransportMode) {
        _state.value = _state.value.copy(selectedTransport = value)
    }

    fun onSearchChange(value: String) {
        _state.value = _state.value.copy(
            searchQuery = value,
            filteredPois = filterPois(_state.value.availablePois, value)
        )
    }

    fun togglePoi(poi: POI) {
        val current = _state.value.selectedPois
        val updated = if (current.any { it.id == poi.id }) {
            current.filterNot { it.id == poi.id }
        } else {
            current + poi
        }

        _state.value = _state.value.copy(selectedPois = updated)
    }

    fun removePoi(poiId: Int) {
        _state.value = _state.value.copy(
            selectedPois = _state.value.selectedPois.filterNot { it.id == poiId }
        )
    }

    fun createRoute(cityId: Int) {
        val current = _state.value
        if (current.routeName.isBlank()) {
            _state.value = current.copy(errorMessage = "Введите название маршрута")
            return
        }
        if (current.selectedPois.isEmpty()) {
            _state.value = current.copy(errorMessage = "Выберите хотя бы одну точку")
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(isLoading = true, errorMessage = null)

            runCatching {
                repository.createRoute(
                    cityId = cityId,
                    name = current.routeName.trim(),
                    description = current.routeDescription.trim().ifBlank { null },
                    transportMode = current.selectedTransport,
                    selectedPois = current.selectedPois
                )
            }.onSuccess { route ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    createdRouteId = route.id,
                    errorMessage = null
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Не удалось создать маршрут"
                )
            }
        }
    }

    private fun filterPois(source: List<POI>, query: String): List<POI> {
        if (query.isBlank()) return source
        return source.filter { poi ->
            poi.name.contains(query, ignoreCase = true) ||
                    (poi.address?.contains(query, ignoreCase = true) == true)
        }
    }

    fun preselectPoiIfNeeded(poiId: Int) {
        val current = _state.value
        if (current.selectedPois.any { it.id == poiId }) return

        val poi = current.availablePois.firstOrNull { it.id == poiId } ?: return

        _state.value = current.copy(
            selectedPois = current.selectedPois + poi
        )
    }
}