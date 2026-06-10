package com.travelguide.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.travelguide.AppContainer
import com.travelguide.core.toUserMessage
import com.travelguide.domain.models.TransportMode
import com.travelguide.ui.screens.route.RouteMapScreen

@Composable
fun OneTimePoiRouteMapRoute(
    container: AppContainer,
    cityId: Int,
    poiId: Int,
    fromLatitude: Double,
    fromLongitude: Double,
    transportMode: TransportMode,
    onBackClick: () -> Unit,
    onOpenPoi: (Int) -> Unit
) {
    var state by remember { mutableStateOf(RouteMapUiState(isLoading = true)) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(cityId, poiId, fromLatitude, fromLongitude, transportMode, reloadKey) {
        state = RouteMapUiState(isLoading = true)
        runCatching {
            container.routeRepository.buildOneTimeRouteToPoi(
                cityId = cityId,
                toPoiId = poiId,
                fromLatitude = fromLatitude,
                fromLongitude = fromLongitude,
                fromTitle = "Выбранная точка старта",
                transportMode = transportMode
            )
        }.onSuccess { map ->
            val firstDay = map.days.firstOrNull()
            val firstPoint = firstDay?.points?.firstOrNull()
            state = RouteMapUiState(
                isLoading = false,
                routeMap = map,
                selectedDayNumber = firstDay?.dayNumber,
                selectedPointId = firstPoint?.routePointId
            )
        }.onFailure { error ->
            state = RouteMapUiState(
                isLoading = false,
                errorMessage = error.toUserMessage("Не удалось построить маршрут до объекта")
            )
        }
    }

    RouteMapScreen(
        state = state,
        onRetry = {
            reloadKey++
        },
        onBackClick = onBackClick,
        onViewList = onBackClick,
        onDaySelected = { dayNumber -> state = state.copy(selectedDayNumber = dayNumber) },
        onPointSelected = { pointId -> state = state.copy(selectedPointId = pointId) },
        onOpenPoi = onOpenPoi,
        showOpenPoiAction = false
    )
}
