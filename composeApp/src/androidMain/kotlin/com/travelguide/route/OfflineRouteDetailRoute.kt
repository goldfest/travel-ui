package com.travelguide.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.ui.screens.route.RouteDetailScreen

@Composable
fun OfflineRouteDetailRoute(
    container: AppContainer,
    routeId: Int,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onViewMap: () -> Unit
) {
    val vm: RouteDetailViewModel = viewModel(
        key = "offline-route-detail-$routeId",
        factory = SimpleViewModelFactory {
            RouteDetailViewModel(container.routeRepository)
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(routeId) {
        vm.loadOfflineRoute(routeId)
    }

    RouteDetailScreen(
        route = state.route,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        isOfflineMode = true,
        isOfflineAvailable = true,
        isOptimizing = state.isOptimizing,
        onRetry = { vm.loadOfflineRoute(routeId) },
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        onViewMap = onViewMap,
        onViewList = {},
        onOptimizeClick = {},
        onDownloadOfflineClick = {},
        onExportPdfClick = {},
        onExportGpxClick = {},
        onExportJsonClick = {},
        onExportOfflineArchiveClick = {}
    )
}