package com.travelguide.city

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.city.CityInfoScreen

@Composable
fun CityInfoRoute(
    container: AppContainer,
    cityId: Int,
    onPOIClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    val vm: CityViewModel = viewModel(
        key = "city-$cityId",
        factory = SimpleViewModelFactory { CityViewModel(container.cityRepository) }
    )

    val state by vm.detailsState.collectAsState()

    LaunchedEffect(cityId) {
        vm.loadCity(cityId)
    }

    CityInfoScreen(
        city = state.city,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRetry = { vm.loadCity(cityId) },
        onPOIClick = onPOIClick,
        onBackClick = onBackClick,
        onFilterClick = onFilterClick
    )
}