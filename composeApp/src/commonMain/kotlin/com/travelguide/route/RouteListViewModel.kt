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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RouteListViewModel(
    private val repository: RouteRepository
) : ViewModel() {

    private var refreshJob: Job? = null
    private var syncWatcherJob: Job? = null
    private var syncRefreshJob: Job? = null
    private var syncMessageCounter: Long = 0L
    private var lastPendingSyncState: Boolean? = null

    private val _state = MutableStateFlow(RouteListUiState())
    val state: StateFlow<RouteListUiState> = _state.asStateFlow()


    fun loadRoutes(filter: RouteListFilter = _state.value.filter, silent: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = if (silent) _state.value.isLoading else true,
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
                    routeIdsWithDrafts = repository.getRouteIdsWithSavedEditorDrafts(),
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


    fun onNetworkRestored() {
        viewModelScope.launch {
            if (_state.value.showApplyDraftsDialog || _state.value.isApplyingDrafts) return@launch
            if (repository.hasAnySavedEditorDrafts()) {
                _state.value = _state.value.copy(showApplyDraftsDialog = true)
            }
        }
    }

    fun dismissApplyDraftsDialog() {
        _state.value = _state.value.copy(showApplyDraftsDialog = false)
    }

    fun applySavedDrafts() {
        viewModelScope.launch {
            val syncingRouteIds = repository.getRouteIdsWithSavedEditorDrafts()
            _state.value = _state.value.copy(
                showApplyDraftsDialog = false,
                isApplyingDrafts = true,
                syncingRouteIds = syncingRouteIds,
                errorMessage = null
            )

            runCatching { repository.applySavedEditorDrafts() }
                .onSuccess { appliedCount ->
                    loadRoutes(_state.value.filter, silent = true)
                    _state.value = _state.value.copy(
                        isApplyingDrafts = false,
                        syncingRouteIds = emptySet(),
                        routeIdsWithDrafts = repository.getRouteIdsWithSavedEditorDrafts()
                    ).withSyncMessage(
                        nextSyncMessageId(),
                        if (appliedCount > 0) {
                            "Изменения из оффлайн-черновиков успешно применены."
                        } else {
                            "Сохранённых черновиков для применения не найдено."
                        }
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isApplyingDrafts = false,
                        syncingRouteIds = emptySet(),
                        errorMessage = e.toUserMessage("Не удалось применить оффлайн-изменения")
                    )
                }
        }
    }


    private fun startAutoRefreshWhileSyncPending() {
        if (syncRefreshJob?.isActive == true) return
        syncRefreshJob = viewModelScope.launch {
            while (true) {
                delay(4000)
                loadRoutes(_state.value.filter, silent = true)
            }
        }
    }

    private fun stopAutoRefreshWhileSyncPending() {
        syncRefreshJob?.cancel()
        syncRefreshJob = null
    }

    private fun scheduleRefreshIfNeeded(filter: RouteListFilter, routes: List<Route>) {
        refreshJob?.cancel()
        if (filter != RouteListFilter.ACTIVE || routes.none { it.status == RouteStatus.GRAPH_PREPARING }) {
            return
        }

        refreshJob = viewModelScope.launch {
            delay(4000)
            loadRoutes(filter, silent = true)
        }
    }

    private fun nextSyncMessageId(): Long {
        syncMessageCounter += 1L
        return syncMessageCounter
    }
}

private fun RouteListUiState.withSyncMessage(messageId: Long, message: String): RouteListUiState {
    return copy(
        syncStatusMessage = message,
        syncStatusMessageId = messageId
    )
}
