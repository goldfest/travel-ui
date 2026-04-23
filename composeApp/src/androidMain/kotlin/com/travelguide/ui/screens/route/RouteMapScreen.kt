package com.travelguide.ui.screens.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.RouteMapPoint
import com.travelguide.route.RouteMapUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMapScreen(
    state: RouteMapUiState,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onViewList: () -> Unit,
    onDaySelected: (Int) -> Unit,
    onPointSelected: (Int) -> Unit,
    onOpenPoi: (Int) -> Unit = {},
    showOpenPoiAction: Boolean = true
) {
    val routeMap = state.routeMap
    val selectedDay = routeMap?.days?.firstOrNull { it.dayNumber == state.selectedDayNumber }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()

    when {
        state.isLoading && routeMap == null -> {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Загрузка карты…", modifier = Modifier.padding(16.dp))
            }
        }

        !state.errorMessage.isNullOrBlank() && routeMap == null -> {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = state.errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                Button(onClick = onRetry, modifier = Modifier.padding(horizontal = 16.dp)) { Text("Повторить") }
            }
        }

        routeMap != null && selectedDay != null -> {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = 78.dp,
                sheetDragHandle = {
                    Text(
                        text = "Точки маршрута",
                        modifier = Modifier.padding(vertical = 10.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                sheetContent = {
                    RoutePointsSheet(
                        points = selectedDay.points,
                        selectedPointId = state.selectedPointId,
                        onPointClick = { pointId ->
                            onPointSelected(pointId)
                            scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                        },
                        onOpenPoi = onOpenPoi,
                        showOpenPoiAction = showOpenPoiAction
                    )
                },
                topBar = {
                    TopAppBar(
                        title = { Text("Карта маршрута") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                            }
                        },
                        actions = {
                            IconButton(onClick = onViewList) {
                                Icon(Icons.Default.Route, contentDescription = "Список")
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RouteSummaryCard(
                        title = routeMap.routeName,
                        distanceKm = routeMap.totalDistanceKm,
                        durationMin = routeMap.totalDurationMin,
                        transport = routeMap.transportMode,
                        pointsCount = selectedDay.points.size,
                        daysCount = routeMap.days.size
                    )

                    if (routeMap.days.size > 1) {
                        DayDropdownButton(
                            dayNumbers = routeMap.days.map { it.dayNumber },
                            selectedDayNumber = state.selectedDayNumber,
                            onDaySelected = onDaySelected
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = MaterialTheme.shapes.extraLarge,
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            OsmRouteMapView(
                                day = selectedDay,
                                selectedPointId = state.selectedPointId,
                                modifier = Modifier.fillMaxSize(),
                                onPointClick = { pointId ->
                                    onPointSelected(pointId)
                                    scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayDropdownButton(
    dayNumbers: List<Int>,
    selectedDayNumber: Int?,
    onDaySelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }, shape = MaterialTheme.shapes.large) {
            Text(selectedDayNumber?.let { "День $it" } ?: "Выбрать день")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            dayNumbers.forEach { day ->
                DropdownMenuItem(
                    text = { Text("День $day") },
                    onClick = {
                        expanded = false
                        onDaySelected(day)
                    }
                )
            }
        }
    }
}

@Composable
private fun RouteSummaryCard(
    title: String,
    distanceKm: Double?,
    durationMin: Int?,
    transport: String,
    pointsCount: Int,
    daysCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryChip("${distanceKm ?: 0.0} км")
                SummaryChip("${durationMin ?: 0} мин")
                SummaryChip(transport)
            }
            Text(
                "$pointsCount точек • $daysCount дней",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryChip(text: String) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun RoutePointsSheet(
    points: List<RouteMapPoint>,
    selectedPointId: Int?,
    onPointClick: (Int) -> Unit,
    onOpenPoi: (Int) -> Unit,
    showOpenPoiAction: Boolean
) {
    LazyColumn(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        itemsIndexed(points) { index, point ->
            val selected = point.routePointId == selectedPointId

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                onClick = { onPointClick(point.routePointId) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primary) {
                        Text(
                            text = (index + 1).toString(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(point.poiName.orEmpty(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                        point.poiAddress?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        point.estimatedVisitMinutes?.let {
                            Text("Посещение: $it мин", style = MaterialTheme.typography.bodySmall)
                        }
                        if (showOpenPoiAction) {
                            Button(onClick = { onOpenPoi(point.poiId) }) {
                                Text("Открыть объект")
                            }
                        }
                    }
                }
            }
        }
    }
}
