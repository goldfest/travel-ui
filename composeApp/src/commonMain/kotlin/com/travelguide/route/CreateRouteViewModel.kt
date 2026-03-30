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

    fun selectDay(dayNumber: Int) {
        _state.value = _state.value.copy(selectedDayNumber = dayNumber)
    }

    fun addDay() {
        val current = _state.value
        val nextDayNumber = (current.days.maxOfOrNull { it.dayNumber } ?: 0) + 1

        _state.value = current.copy(
            days = current.days + EditableRouteDayUi(dayNumber = nextDayNumber),
            selectedDayNumber = nextDayNumber
        )
    }

    fun removeDay(dayNumber: Int) {
        val current = _state.value
        if (current.days.size <= 1) return

        val updated = current.days
            .filterNot { it.dayNumber == dayNumber }
            .mapIndexed { index, day ->
                day.copy(dayNumber = index + 1)
            }

        _state.value = current.copy(
            days = updated,
            selectedDayNumber = updated.firstOrNull()?.dayNumber ?: 1
        )
    }

    fun onDayDescriptionChange(dayNumber: Int, value: String) {
        _state.value = _state.value.copy(
            days = _state.value.days.map { day ->
                if (day.dayNumber == dayNumber) {
                    day.copy(description = value)
                } else {
                    day
                }
            }
        )
    }

    fun addPoiToSelectedDay(poi: POI) {
        val current = _state.value
        val selectedDayNumber = current.selectedDayNumber

        val updatedDays = current.days.map { day ->
            if (day.dayNumber != selectedDayNumber) return@map day
            if (day.points.any { it.poi.id == poi.id }) return@map day

            day.copy(
                points = day.points + EditableRoutePointUi(poi = poi)
            )
        }

        _state.value = current.copy(days = updatedDays)
    }

    fun removePoiFromDay(dayNumber: Int, poiId: Int) {
        val current = _state.value

        val updatedDays = current.days.map { day ->
            if (day.dayNumber != dayNumber) return@map day

            day.copy(
                points = day.points.filterNot { it.poi.id == poiId }
            )
        }

        _state.value = current.copy(days = updatedDays)
    }

    fun updateVisitMinutes(dayNumber: Int, poiId: Int, minutes: Int) {
        val normalized = minutes.coerceAtLeast(5)

        _state.value = _state.value.copy(
            days = _state.value.days.map { day ->
                if (day.dayNumber != dayNumber) return@map day

                day.copy(
                    points = day.points.map { point ->
                        if (point.poi.id == poiId) {
                            point.copy(estimatedVisitMinutes = normalized)
                        } else {
                            point
                        }
                    }
                )
            }
        )
    }

    fun createRoute(cityId: Int) {
        val current = _state.value

        if (current.routeName.isBlank()) {
            _state.value = current.copy(errorMessage = "Введите название маршрута")
            return
        }

        if (current.days.none { it.points.isNotEmpty() }) {
            _state.value = current.copy(errorMessage = "Добавьте хотя бы одну точку в маршрут")
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(isSaving = true, errorMessage = null)

            runCatching {
                repository.createRoute(
                    cityId = cityId,
                    name = current.routeName.trim(),
                    description = current.routeDescription.trim().ifBlank { null },
                    transportMode = current.selectedTransport,
                    days = current.days
                )
            }.onSuccess { route ->
                _state.value = _state.value.copy(
                    isSaving = false,
                    createdRouteId = route.id,
                    errorMessage = null
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Не удалось создать маршрут"
                )
            }
        }
    }

    fun preselectPoiIfNeeded(poiId: Int) {
        val current = _state.value
        val poi = current.availablePois.firstOrNull { it.id == poiId } ?: return
        val selectedDayNumber = current.selectedDayNumber

        val updatedDays = current.days.map { day ->
            if (day.dayNumber != selectedDayNumber) return@map day
            if (day.points.any { it.poi.id == poiId }) return@map day

            day.copy(
                points = day.points + EditableRoutePointUi(poi = poi)
            )
        }

        _state.value = current.copy(days = updatedDays)
    }

    private fun filterPois(source: List<POI>, query: String): List<POI> {
        if (query.isBlank()) return source

        return source.filter { poi ->
            poi.name.contains(query, ignoreCase = true) ||
                    (poi.address?.contains(query, ignoreCase = true) == true)
        }
    }
}