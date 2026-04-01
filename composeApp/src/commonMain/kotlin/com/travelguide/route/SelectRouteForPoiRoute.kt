package com.travelguide.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.travelguide.AppContainer
import com.travelguide.domain.models.Route
import com.travelguide.ui.screens.route.SelectRouteForPoiScreen
import kotlinx.coroutines.launch

@Composable
fun SelectRouteForPoiRoute(
    container: AppContainer,
    cityId: Int,
    poiId: Int,
    onBackClick: () -> Unit,
    onAdded: (Int) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var routes by remember { mutableStateOf<List<Route>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAdding by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            isLoading = true
            errorMessage = null

            runCatching {
                container.routeRepository
                    .getRoutesByCity(cityId)
                    .filter { !it.isArchived }
            }.onSuccess { loaded ->
                routes = loaded
                isLoading = false
            }.onFailure { e ->
                routes = emptyList()
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
            if (isAdding) return@SelectRouteForPoiScreen

            scope.launch {
                isAdding = true
                errorMessage = null

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

                isAdding = false
            }
        }
    )
}