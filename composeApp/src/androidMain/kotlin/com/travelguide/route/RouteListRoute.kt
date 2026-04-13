package com.travelguide.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.collect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.ui.screens.route.RouteListScreen

@Composable
fun RouteListRoute(
    container: AppContainer,
    onBackClick: () -> Unit,
    onRouteClick: (Int, RouteListFilter) -> Unit,
    onCreateRoute: () -> Unit
) {
    val vm: RouteListViewModel = viewModel(
        key = "route-list",
        factory = SimpleViewModelFactory {
            RouteListViewModel(container.routeRepository)
        }
    )

    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.loadRoutes()
    }

    LaunchedEffect(Unit) {
        observeInternetConnectivity(context)
            .drop(1)
            .collect { isConnected ->
                if (isConnected) {
                    vm.onNetworkRestored()
                }
            }
    }

    LaunchedEffect(state.syncStatusMessageId) {
        val message = state.syncStatusMessage
        if (state.syncStatusMessageId != 0L && !message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
        }
    }

    RouteListScreen(
        routes = state.routes,
        isLoading = state.isLoading,
        filter = state.filter,
        deletingRouteId = state.deletingRouteId,
        errorMessage = state.errorMessage,
        routeIdsWithDrafts = state.routeIdsWithDrafts,
        syncingRouteIds = state.syncingRouteIds,
        showApplyDraftsDialog = state.showApplyDraftsDialog,
        isApplyingDrafts = state.isApplyingDrafts,
        snackbarHostState = snackbarHostState,
        onRetry = { vm.loadRoutes(state.filter) },
        onBackClick = onBackClick,
        onFilterChange = vm::setFilter,
        onRouteClick = { routeId -> onRouteClick(routeId, state.filter) },
        onDeleteRoute = vm::deleteRoute,
        onArchiveRoute = vm::archiveRoute,
        onUnarchiveRoute = vm::unarchiveRoute,
        onCreateRoute = onCreateRoute,
        onApplyDrafts = vm::applySavedDrafts,
        onDismissApplyDraftsDialog = vm::dismissApplyDraftsDialog
    )
}