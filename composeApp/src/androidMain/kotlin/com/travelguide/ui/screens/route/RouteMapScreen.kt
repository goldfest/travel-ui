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
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.RouteMapPoint
import com.travelguide.route.RouteMapUiState
import kotlinx.coroutines.launch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMapScreen(
    state: RouteMapUiState,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onViewList: () -> Unit,
    onDaySelected: (Int) -> Unit,
    onPointSelected: (Int) -> Unit,
    onOpenPoi: (Int) -> Unit = {}
) {
    val routeMap = state.routeMap
    val selectedDay = routeMap?.days?.firstOrNull { it.dayNumber == state.selectedDayNumber }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()

    when {
        state.isLoading && routeMap == null -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Загрузка карты…", modifier = Modifier.padding(16.dp))
            }
        }

        !state.errorMessage.isNullOrBlank() && routeMap == null -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                Button(
                    onClick = onRetry,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text("Повторить")
                }
            }
        }

        routeMap != null && selectedDay != null -> {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = 72.dp,
                sheetDragHandle = {
                    Text(
                        text = "Точки маршрута",
                        modifier = Modifier.padding(vertical = 8.dp),
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
                            scope.launch {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        },
                        onOpenPoi = onOpenPoi
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
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        if (scaffoldState.bottomSheetState.currentValue ==
                                            androidx.compose.material3.SheetValue.Expanded
                                        ) {
                                            scaffoldState.bottomSheetState.partialExpand()
                                        } else {
                                            scaffoldState.bottomSheetState.expand()
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.List, contentDescription = "Точки маршрута")
                            }
                        }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    YandexRouteMapView(
                        day = selectedDay,
                        selectedPointId = state.selectedPointId,
                        modifier = Modifier.fillMaxSize(),
                        onPointClick = { pointId ->
                            onPointSelected(pointId)
                            scope.launch {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
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
                            DayDropdownSelector(
                                dayNumbers = routeMap.days.map { it.dayNumber },
                                selectedDayNumber = state.selectedDayNumber,
                                onDaySelected = onDaySelected
                            )
                        }
                    }
                }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "${distanceKm ?: 0.0} км • ${durationMin ?: 0} мин • $transport",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "$pointsCount точек • $daysCount дней",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDropdownSelector(
    dayNumbers: List<Int>,
    selectedDayNumber: Int?,
    onDaySelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedDayNumber?.let { "День $it" }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text("День маршрута") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
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
private fun RoutePointsSheet(
    points: List<RouteMapPoint>,
    selectedPointId: Int?,
    onPointClick: (Int) -> Unit,
    onOpenPoi: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        itemsIndexed(points) { index, point ->
            val selected = point.routePointId == selectedPointId

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                ),
                onClick = { onPointClick(point.routePointId) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "${index + 1}. ${point.poiName.orEmpty()}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    point.poiAddress?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    point.estimatedVisitMinutes?.let {
                        Text(
                            "Посещение: $it мин",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = { onOpenPoi(point.poiId) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Открыть объект")
                    }
                }
            }
        }
    }
}