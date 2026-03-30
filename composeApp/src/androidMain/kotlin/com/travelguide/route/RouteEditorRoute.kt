package com.travelguide.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.route.RouteEditorScreen

@Composable
fun RouteEditorRoute(
    container: AppContainer,
    routeId: Int?,
    cityId: Int?,
    initialPoiId: Int?,
    onBackClick: () -> Unit,
    onSaved: (Int) -> Unit
) {
    val vm: RouteEditorViewModel = viewModel(
        key = "route-editor-${routeId ?: "new"}-${cityId ?: "none"}-${initialPoiId ?: "none"}",
        factory = SimpleViewModelFactory {
            RouteEditorViewModel(
                routeRepository = container.routeRepository,
                cityRepository = container.cityRepository
            )
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(routeId, cityId, initialPoiId) {
        if (routeId != null) {
            vm.startEdit(routeId)
        } else {
            vm.startCreate(cityId, initialPoiId)
        }
    }

    LaunchedEffect(state.savedRouteId) {
        state.savedRouteId?.let(onSaved)
    }

    RouteEditorScreen(
        mode = state.mode,
        availableCities = state.availableCities,
        selectedCityId = state.selectedCityId,
        selectedCityName = state.selectedCityName,
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
        onCitySelected = vm::onCitySelected,
        onNameChange = vm::onNameChange,
        onDescriptionChange = vm::onDescriptionChange,
        onTransportChange = vm::onTransportChange,
        onSearchChange = vm::onSearchChange,
        onSelectDay = vm::selectDay,
        onAddDay = vm::addDay,
        onRemoveDay = vm::removeDay,
        onDayDescriptionChange = vm::onDayDescriptionChange,
        onAddPoiToDay = vm::addPoiToSelectedDay,
        onRemovePoiFromDay = vm::removePoiFromDay,
        onMovePoint = vm::movePoint,
        onSaveClick = vm::save
    )
}