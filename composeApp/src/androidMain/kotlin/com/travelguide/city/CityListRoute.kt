package com.travelguide.city

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.city.CityListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityListRoute(
    container: AppContainer,
    onCityClick: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val vm: CityViewModel = viewModel(
        factory = SimpleViewModelFactory { CityViewModel(container.cityRepository) }
    )

    val state by vm.listState.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadCities()
    }

    CityListScreen(
        cities = state.cities,
        popularCities = state.popularCities,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRetry = { vm.loadCities() },
        onSearch = { vm.searchCities(it) },
        onSearchClick = onSearchClick,
        onCityClick = onCityClick,
        onProfileClick = onProfileClick,
        onNotificationsClick = onNotificationsClick
    )
}