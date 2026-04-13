package com.travelguide.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.core.toUserMessage
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RouteListViewModel(
    private val repository: RouteRepository
) : ViewModel() {

    private var refreshJob: Job? = null

    private val _state = MutableStateFlow(RouteListUiState())
    val state: StateFlow<RouteListUiState> = _state.asStateFlow()

    fun loadRoutes(filter: RouteListFilter = _state.value.filter) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null,
                filter = filter
            )

            runCatching {
                when (filter) {
                    RouteListFilter.ACTIVE -> repository.getRoutes(archived = false)
                    RouteListFilter.ARCHIVED -> repository.getRoutes(archived = true)
                    RouteListFilter.OFFLINE -> repository.getOfflineRoutes()
                }
            }.onSuccess { routes ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    routes = routes,
                    errorMessage = null
                )
                scheduleRefreshIfNeeded(filter, routes)
            }.onFailure { e ->
                refreshJob?.cancel()
                _state.value = _state.value.copy(
                    isLoading = false,
                    routes = if (filter == RouteListFilter.OFFLINE) _state.value.routes else emptyList(),
                    errorMessage = e.toUserMessage("Не удалось загрузить маршруты")
                )
            }
        }
    }

    fun setFilter(filter: RouteListFilter) {
        if (_state.value.filter == filter) return
        loadRoutes(filter)
    }

    fun deleteRoute(routeId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(deletingRouteId = routeId, errorMessage = null)

            runCatching {
                when (_state.value.filter) {
                    RouteListFilter.OFFLINE -> repository.deleteOfflineRoute(routeId)
                    else -> repository.deleteRoute(routeId)
                }
            }.onSuccess {
                _state.value = _state.value.copy(deletingRouteId = null)
                loadRoutes(_state.value.filter)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    deletingRouteId = null,
                    errorMessage = e.toUserMessage(
                        if (_state.value.filter == RouteListFilter.OFFLINE)
                            "Не удалось удалить маршрут с устройства"
                        else
                            "Не удалось удалить маршрут"
                    )
                )
            }
        }
    }

    fun archiveRoute(routeId: Int) {
        viewModelScope.launch {
            runCatching { repository.archiveRoute(routeId) }
                .onSuccess { loadRoutes(_state.value.filter) }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        errorMessage = e.toUserMessage("Не удалось переместить маршрут в архив")
                    )
                }
        }
    }

    fun unarchiveRoute(routeId: Int) {
        viewModelScope.launch {
            runCatching { repository.unarchiveRoute(routeId) }
                .onSuccess { loadRoutes(_state.value.filter) }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        errorMessage = e.toUserMessage("Не удалось вернуть маршрут из архива")
                    )
                }
        }
    }

    private fun scheduleRefreshIfNeeded(filter: RouteListFilter, routes: List<Route>) {
        refreshJob?.cancel()
        if (filter != RouteListFilter.ACTIVE || routes.none { it.status == RouteStatus.GRAPH_PREPARING }) {
            return
        }

        refreshJob = viewModelScope.launch {
            delay(4000)
            loadRoutes(filter)
        }
    }
}