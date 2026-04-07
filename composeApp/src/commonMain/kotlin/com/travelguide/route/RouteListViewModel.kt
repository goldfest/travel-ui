package com.travelguide.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun loadRoutes(showArchived: Boolean = _state.value.showArchived) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null,
                showArchived = showArchived
            )

            runCatching {
                repository.getRoutes(archived = showArchived)
            }.onSuccess { routes ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    routes = routes,
                    errorMessage = null
                )
                scheduleRefreshIfNeeded(showArchived, routes)
            }.onFailure { e ->
                refreshJob?.cancel()
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Не удалось загрузить маршруты"
                )
            }
        }
    }

    fun toggleArchiveFilter() {
        loadRoutes(showArchived = !_state.value.showArchived)
    }

    private fun scheduleRefreshIfNeeded(showArchived: Boolean, routes: List<com.travelguide.domain.models.Route>) {
        refreshJob?.cancel()
        if (showArchived || routes.none { it.status == com.travelguide.domain.models.RouteStatus.GRAPH_PREPARING }) {
            return
        }

        refreshJob = viewModelScope.launch {
            delay(4000)
            loadRoutes(showArchived)
        }
    }
}