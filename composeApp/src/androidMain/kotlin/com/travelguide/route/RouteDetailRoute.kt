package com.travelguide.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.route.RouteDetailScreen

@Composable
fun RouteDetailRoute(
    container: AppContainer,
    routeId: Int,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onViewMap: () -> Unit,
    onViewList: () -> Unit
) {
    val vm: RouteDetailViewModel = viewModel(
        key = "route-detail-$routeId",
        factory = SimpleViewModelFactory {
            RouteDetailViewModel(container.routeRepository)
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(routeId) {
        vm.loadRoute(routeId)
    }

    RouteDetailScreen(
        route = state.route,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRetry = { vm.loadRoute(routeId) },
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        onViewMap = onViewMap,
        onViewList = onViewList,
        onOptimizeClick = { vm.optimize(routeId) }
    )
}