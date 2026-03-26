package com.travelguide.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.route.RouteMapScreen

@Composable
fun RouteMapRoute(
    container: AppContainer,
    routeId: Int,
    onBackClick: () -> Unit,
    onViewList: () -> Unit
) {
    val vm: RouteDetailViewModel = viewModel(
        key = "route-map-$routeId",
        factory = SimpleViewModelFactory {
            RouteDetailViewModel(container.routeRepository)
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(routeId) {
        vm.loadRoute(routeId)
    }

    RouteMapScreen(
        route = state.route,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRetry = { vm.loadRoute(routeId) },
        onBackClick = onBackClick,
        onViewList = onViewList
    )
}