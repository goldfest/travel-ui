package com.travelguide.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.route.CreateRouteScreen

@Composable
fun CreateRouteRoute(
    container: AppContainer,
    cityId: Int,
    initialPoiId: Int? = null,
    onBackClick: () -> Unit,
    onSubmit: (Int) -> Unit
) {
    val vm: CreateRouteViewModel = viewModel(
        key = "create-route-$cityId",
        factory = SimpleViewModelFactory {
            CreateRouteViewModel(container.routeRepository)
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(cityId) {
        vm.loadPois(cityId)
    }

    LaunchedEffect(state.createdRouteId) {
        state.createdRouteId?.let(onSubmit)
    }
    LaunchedEffect(state.availablePois, initialPoiId) {
        val poiId = initialPoiId ?: return@LaunchedEffect
        if (state.availablePois.isNotEmpty()) {
            vm.preselectPoiIfNeeded(poiId)
        }
    }

    CreateRouteScreen(
        routeName = state.routeName,
        routeDescription = state.routeDescription,
        selectedTransport = state.selectedTransport,
        searchQuery = state.searchQuery,
        availablePois = state.filteredPois,
        selectedPois = state.selectedPois,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onBackClick = onBackClick,
        onNameChange = vm::onNameChange,
        onDescriptionChange = vm::onDescriptionChange,
        onTransportChange = vm::onTransportChange,
        onSearchChange = vm::onSearchChange,
        onTogglePoi = vm::togglePoi,
        onRemovePoi = { vm.removePoi(it) },
        onSubmitClick = { vm.createRoute(cityId) }
    )
}