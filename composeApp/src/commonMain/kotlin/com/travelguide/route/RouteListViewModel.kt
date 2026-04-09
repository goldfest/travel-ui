package com.travelguide.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.domain.models.RouteStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.travelguide.core.toUserMessage
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
            _state.value = _state.value.copy(
                deletingRouteId = routeId,
                errorMessage = null
            )

            runCatching {
                repository.deleteRoute(routeId)
            }.onSuccess {
                _state.value = _state.value.copy(deletingRouteId = null)
                loadRoutes(_state.value.filter)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    deletingRouteId = null,
                    errorMessage = e.toUserMessage("Не удалось удалить маршрут")
                )
            }
        }
    }

    private fun scheduleRefreshIfNeeded(filter: RouteListFilter, routes: List<com.travelguide.domain.models.Route>) {
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