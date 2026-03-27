package com.travelguide.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.travelguide.AppContainer
import com.travelguide.ui.screens.route.RouteMapScreen

@Composable
fun RouteMapRoute(
    container: AppContainer,
    routeId: Int,
    onBackClick: () -> Unit,
    onViewList: () -> Unit
) {
    val viewModel = remember { RouteMapViewModel(container.routeRepository) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(routeId) {
        viewModel.loadRouteMap(routeId)
    }

    RouteMapScreen(
        state = state,
        onRetry = { viewModel.loadRouteMap(routeId) },
        onBackClick = onBackClick,
        onViewList = onViewList,
        onDaySelected = viewModel::selectDay
    )
}