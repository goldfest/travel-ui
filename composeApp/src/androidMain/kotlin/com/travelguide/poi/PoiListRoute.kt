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
                favoriteRepository = container.favoriteRepository,
                reviewRepository = container.reviewRepository
            )
        }
    )

    val state by vm.listState.collectAsState()

    LaunchedEffect(cityId) {
        vm.loadPoisByCity(cityId)
    }

    POIListScreen(
        cityId = cityId,
        items = state.items,
        poiTypes = state.poiTypes,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        currentPage = state.currentPage,
        totalPages = state.totalPages,
        onRetry = { vm.loadPoisByCity(cityId, page = state.currentPage) },
        onSearch = { query, typeCode ->
            val selectedTypeIds = state.poiTypes
                .filter { it.code == typeCode }
                .map { it.id }

            if (query.isBlank() && typeCode == null) {
                vm.loadPoisByCity(cityId, page = 0)
            } else {
                vm.searchPoisInCity(
                    cityId = cityId,
                    query = query,
                    poiTypeIds = selectedTypeIds,
                    page = 0
                )
            }
        },
        onPageChange = { page, query, typeCode ->
            val selectedTypeIds = state.poiTypes
                .filter { it.code == typeCode }
                .map { it.id }

            if (query.isBlank() && typeCode == null) {
                vm.loadPoisByCity(cityId, page = page)
            } else {
                vm.searchPoisInCity(
                    cityId = cityId,
                    query = query,
                    poiTypeIds = selectedTypeIds,
                    page = page
                )
            }
        },
        onPOIClick = onPOIClick,
        onFavoriteClick = { poiId ->
            vm.toggleFavoriteForCard(poiId)
        },
        onBackClick = onBackClick,
        onFilterClick = onFilterClick
    )
}