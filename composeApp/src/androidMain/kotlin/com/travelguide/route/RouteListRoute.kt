package com.travelguide.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.route.RouteListScreen

@Composable
fun RouteListRoute(
    container: AppContainer,
    onBackClick: () -> Unit,
    onRouteClick: (Int) -> Unit
) {
    val vm: RouteListViewModel = viewModel(
        key = "route-list",
        factory = SimpleViewModelFactory {
            RouteListViewModel(container.routeRepository)
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadRoutes()
    }

    RouteListScreen(
        routes = state.routes,
        isLoading = state.isLoading,
        showArchived = state.showArchived,
        errorMessage = state.errorMessage,
        onRetry = { vm.loadRoutes() },
        onBackClick = onBackClick,
        onToggleArchived = { vm.toggleArchiveFilter() },
        onRouteClick = onRouteClick,
        onCreateRoute = null
    )
}