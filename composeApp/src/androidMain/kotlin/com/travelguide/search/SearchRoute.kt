package com.travelguide.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.search.SearchScreen

@Composable
fun SearchRoute(
    container: AppContainer,
    onBackClick: () -> Unit,
    onPOIClick: (Int) -> Unit
) {
    val vm: SearchViewModel = viewModel(
        factory = SimpleViewModelFactory {
            SearchViewModel(
                cityRepository = container.cityRepository,
                poiRepository = container.poiRepository,
                searchHistoryRepository = container.searchHistoryRepository,
                favoriteRepository = container.favoriteRepository,
                reviewRepository = container.reviewRepository
            )
        }
    )

    val state by vm.state.collectAsState()

    SearchScreen(
        query = state.query,
        cities = state.cities,
        items = state.items,
        selectedCity = state.selectedCity,
        recentSearches = state.recentQueries,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        poiCurrentPage = state.poiPage,
        poiTotalPages = state.poiTotalPages,
        onQueryChange = {
            vm.updateQuery(it)
            vm.search(it)
        },
        onClearQuery = { vm.clearSearch() },
        onSelectRecentQuery = { vm.useRecentQuery(it) },
        onClearHistory = { vm.clearHistory() },
        onSelectCity = { vm.selectCity(it) },
        onPoiPageChange = { page -> vm.loadPoiSearchPage(page) },
        onToggleFavorite = { poiId -> vm.toggleFavorite(poiId) },
        onBackClick = onBackClick,
        onPOIClick = onPOIClick
    )
}