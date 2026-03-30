package com.travelguide.route

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.travelguide.AppContainer
import com.travelguide.domain.models.Route
import com.travelguide.ui.screens.route.SelectRouteForPoiScreen
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition", "UnrememberedMutableState")
@Composable
fun SelectRouteForPoiRoute(
    container: AppContainer,
    cityId: Int,
    poiId: Int,
    onBackClick: () -> Unit,
    onAdded: (Int) -> Unit
) {
    var isLoading by mutableStateOf(true)
    var routes by mutableStateOf<List<Route>>(emptyList())
    var errorMessage by mutableStateOf<String?>(null)

    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            isLoading = true
            errorMessage = null

            runCatching {
                container.routeRepository.getRoutesByCity(cityId)
                    .filter { !it.isArchived }
            }.onSuccess { loaded ->
                routes = loaded
                isLoading = false
            }.onFailure { e ->
                errorMessage = e.message ?: "Не удалось загрузить маршруты"
                isLoading = false
            }
        }
    }

    LaunchedEffect(cityId) {
        reload()
    }

    SelectRouteForPoiScreen(
        routes = routes,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onRetry = { reload() },
        onBackClick = onBackClick,
        onRouteClick = { route ->
            scope.launch {
                runCatching {
                    val targetDay = route.days.maxOfOrNull { it.dayNumber } ?: 1
                    container.routeRepository.addPoiToRoute(
                        routeId = route.id,
                        poiId = poiId,
                        dayNumber = targetDay
                    )
                }.onSuccess { updatedRoute ->
                    onAdded(updatedRoute.id)
                }.onFailure { e ->
                    errorMessage = e.message ?: "Не удалось добавить объект в маршрут"
                }
            }
        }
    )
}