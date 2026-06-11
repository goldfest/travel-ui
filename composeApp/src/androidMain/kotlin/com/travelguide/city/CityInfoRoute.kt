package com.travelguide.city

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.poi.PoiViewModel
import com.travelguide.ui.screens.city.CityInfoScreen

@Composable
fun CityInfoRoute(
    container: AppContainer,
    cityId: Int,
    onPOIClick: (Int) -> Unit,
    onOpenCityMap: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val cityVm: CityViewModel = viewModel(
        key = "city-$cityId",
        factory = SimpleViewModelFactory { CityViewModel(container.cityRepository) }
    )

    val poiVm: PoiViewModel = viewModel(
        key = "city-poi-$cityId",
        factory = SimpleViewModelFactory {
            PoiViewModel(
                repository = container.poiRepository,
                favoriteRepository = container.favoriteRepository,
                reviewRepository = container.reviewRepository
            )
        }
    )

    val cityState by cityVm.detailsState.collectAsState()
    val poiState by poiVm.listState.collectAsState()

    LaunchedEffect(cityId) {
        cityVm.loadCity(cityId)
    }

    CityInfoScreen(
        city = cityState.city,
        isLoading = cityState.isLoading,
        errorMessage = cityState.errorMessage,
        onRetry = { cityVm.loadCity(cityId) },
        items = poiState.items,
        poiTypes = poiState.poiTypes,
        isPoisLoading = poiState.isLoading,
        poisErrorMessage = poiState.errorMessage,
        poiCurrentPage = poiState.currentPage,
        poiTotalPages = poiState.totalPages,
        poiTotalElements = poiState.totalElements,
        onRetryPois = { poiVm.loadPoisByCity(cityId, page = poiState.currentPage) },
        onSearchPois = { query, typeCode ->
            val selectedTypeIds = poiState.poiTypes
                .filter { it.code == typeCode }
                .map { it.id }

            if (query.isBlank() && typeCode == null) {
                poiVm.loadPoisByCity(cityId, page = 0)
            } else {
                poiVm.searchPoisInCity(
                    cityId = cityId,
                    query = query,
                    poiTypeIds = selectedTypeIds,
                    page = 0
                )
            }
        },
        onPoiPageChange = { page, query, typeCode ->
            val selectedTypeIds = poiState.poiTypes
                .filter { it.code == typeCode }
                .map { it.id }

            if (query.isBlank() && typeCode == null) {
                poiVm.loadPoisByCity(cityId, page = page)
            } else {
                poiVm.searchPoisInCity(
                    cityId = cityId,
                    query = query,
                    poiTypeIds = selectedTypeIds,
                    page = page
                )
            }
        },
        onPOIClick = onPOIClick,
        onToggleFavorite = { poiId ->
            poiVm.toggleFavoriteForCard(poiId)
        },
        onBackClick = onBackClick,
        onOpenCityMap = { onOpenCityMap(cityId) }
    )
}