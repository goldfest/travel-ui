package com.travelguide.city

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.ui.screens.city.CityPoiMapScreen

@Composable
fun CityPoiMapRoute(
    container: AppContainer,
    cityId: Int,
    onBackClick: () -> Unit,
    onOpenPoi: (Int) -> Unit
) {
    val vm: CityPoiMapViewModel = viewModel(
        key = "city-poi-map-$cityId",
        factory = com.travelguide.auth.SimpleViewModelFactory {
            CityPoiMapViewModel(
                cityRepository = container.cityRepository,
                poiRepository = container.poiRepository
            )
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(cityId) {
        vm.load(cityId)
    }

    CityPoiMapScreen(
        state = state,
        onRetry = { vm.load(cityId) },
        onBackClick = onBackClick,
        onTypeSelected = vm::selectType,
        onOpenPoi = onOpenPoi
    )
}