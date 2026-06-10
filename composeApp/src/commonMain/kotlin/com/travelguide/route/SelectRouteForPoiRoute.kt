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
    var selectedRouteId by remember { mutableStateOf<Int?>(null) }
    var selectedDayNumber by remember { mutableStateOf<Int?>(null) }
    var selectedOrderIndex by remember { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()

    fun defaultOrderFor(route: Route, dayNumber: Int?): Int {
        val day = route.days.firstOrNull { it.dayNumber == dayNumber }
        return (day?.points?.size ?: 0) + 1
    }

    fun selectRoute(route: Route) {
        val firstDay = route.days.sortedBy { it.dayNumber }.firstOrNull()
        selectedRouteId = route.id
        selectedDayNumber = firstDay?.dayNumber ?: 1
        selectedOrderIndex = defaultOrderFor(route, selectedDayNumber)
    }

    fun reload() {
        scope.launch {
            isLoading = true
            errorMessage = null

            runCatching {
                container.routeRepository
                    .getRoutesByCity(cityId)
                    .filter { !it.isArchived }
                    .sortedByDescending { it.id }
            }.onSuccess { loaded ->
                routes = loaded
                isLoading = false
                val selected = loaded.firstOrNull { it.id == selectedRouteId }
                if (selected == null) {
                    selectedRouteId = null
                    selectedDayNumber = null
                    selectedOrderIndex = null
                }
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
        isAdding = isAdding,
        selectedRouteId = selectedRouteId,
        selectedDayNumber = selectedDayNumber,
        selectedOrderIndex = selectedOrderIndex,
        onRetry = { reload() },
        onBackClick = onBackClick,
        onRouteSelected = { route -> selectRoute(route) },
        onDaySelected = { route, dayNumber ->
            selectedRouteId = route.id
            selectedDayNumber = dayNumber
            selectedOrderIndex = defaultOrderFor(route, dayNumber)
        },
        onOrderSelected = { orderIndex ->
            selectedOrderIndex = orderIndex
        },
        onConfirmAdd = { route ->
            if (isAdding) return@SelectRouteForPoiScreen
            val dayNumber = selectedDayNumber ?: return@SelectRouteForPoiScreen
            val orderIndex = selectedOrderIndex ?: defaultOrderFor(route, dayNumber)

            scope.launch {
                isAdding = true
                errorMessage = null

                runCatching {
                    container.routeRepository.addPoiToRoute(
                        routeId = route.id,
                        poiId = poiId,
                        dayNumber = dayNumber,
                        orderIndex = orderIndex
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
