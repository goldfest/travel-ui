package com.travelguide.poi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.poi.POIListScreen

@Composable
fun POIListRoute(
    container: AppContainer,
    cityId: Int,
    onPOIClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    val vm: PoiViewModel = viewModel(
        key = "poi-list-$cityId",
        factory = SimpleViewModelFactory {
            PoiViewModel(
                repository = container.poiRepository,
                favoriteRepository = container.favoriteRepository
            )
        }
    )

    val state by vm.listState.collectAsState()

    LaunchedEffect(cityId) {
        vm.loadPoisByCity(cityId)
    }

    POIListScreen(
        cityId = cityId,
        pois = state.pois,
        poiTypes = state.poiTypes,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRetry = { vm.loadPoisByCity(cityId) },
        onSearch = { query, typeCode ->
            val selectedTypeIds = state.poiTypes
                .filter { it.code == typeCode }
                .map { it.id }

            if (query.isBlank() && typeCode == null) {
                vm.loadPoisByCity(cityId)
            } else {
                vm.searchPoisInCity(
                    cityId = cityId,
                    query = query,
                    poiTypeIds = selectedTypeIds
                )
            }
        },
        onPOIClick = onPOIClick,
        onBackClick = onBackClick,
        onFilterClick = onFilterClick
    )
}