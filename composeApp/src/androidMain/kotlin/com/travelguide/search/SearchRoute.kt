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
    onPOIClick: (Int) -> Unit,
    onCityClick: (Int) -> Unit
) {
    val vm: SearchViewModel = viewModel(
        factory = SimpleViewModelFactory {
            SearchViewModel(
                cityRepository = container.cityRepository,
                poiRepository = container.poiRepository
            )
        }
    )

    val state by vm.state.collectAsState()

    SearchScreen(
        query = state.query,
        cities = state.cities,
        pois = state.pois,
        selectedCity = state.selectedCity,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onQueryChange = {
            vm.updateQuery(it)
            vm.search(it)
        },
        onClearQuery = { vm.clearSearch() },
        onSelectCity = { vm.selectCity(it) },
        onBackClick = onBackClick,
        onPOIClick = onPOIClick,
        onCityClick = onCityClick
    )
}