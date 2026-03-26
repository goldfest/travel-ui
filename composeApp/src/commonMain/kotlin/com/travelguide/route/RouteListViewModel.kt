package com.travelguide.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RouteListViewModel(
    private val repository: RouteRepository
) : ViewModel() {

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
            }.onFailure { e ->
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
}