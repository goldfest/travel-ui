package com.travelguide.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.travelguide.AppContainer
import com.travelguide.ui.screens.route.CreateRouteScreen

@Composable
fun CreateRouteRoute(
    container: AppContainer,
    cityId: Int,
    initialPoiId: Int?,
    onBackClick: () -> Unit,
    onSubmit: (Int) -> Unit
) {
    val viewModel = remember { CreateRouteViewModel(container.routeRepository) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(cityId) {
        viewModel.loadPois(cityId)
    }

    LaunchedEffect(state.availablePois, initialPoiId) {
        if (initialPoiId != null && state.availablePois.isNotEmpty()) {
            viewModel.preselectPoiIfNeeded(initialPoiId)
        }
    }

    LaunchedEffect(state.createdRouteId) {
        state.createdRouteId?.let(onSubmit)
    }

    CreateRouteScreen(
        routeName = state.routeName,
        routeDescription = state.routeDescription,
        selectedTransport = state.selectedTransport,
        searchQuery = state.searchQuery,
        availablePois = state.filteredPois,
        days = state.days,
        selectedDayNumber = state.selectedDayNumber,
        isLoading = state.isLoading,
        isSaving = state.isSaving,
        errorMessage = state.errorMessage,
        onBackClick = onBackClick,
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onTransportChange = viewModel::onTransportChange,
        onSearchChange = viewModel::onSearchChange,
        onSelectDay = viewModel::selectDay,
        onAddDay = viewModel::addDay,
        onRemoveDay = viewModel::removeDay,
        onDayDescriptionChange = viewModel::onDayDescriptionChange,
        onAddPoiToDay = viewModel::addPoiToSelectedDay,
        onRemovePoiFromDay = viewModel::removePoiFromDay,
        onSubmitClick = { viewModel.createRoute(cityId) }
    )
}