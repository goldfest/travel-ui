package com.travelguide.ui.screens.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelguide.route.RouteMapUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMapScreen(
    state: RouteMapUiState,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onViewList: () -> Unit,
    onDaySelected: (Int) -> Unit
) {
    val routeMap = state.routeMap
    val selectedDay = routeMap?.days?.firstOrNull { it.dayNumber == state.selectedDayNumber }

    Scaffold(
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
                        Icon(Icons.Default.List, contentDescription = "Список")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading && routeMap == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Загрузка карты…", modifier = Modifier.padding(16.dp))
                }
            }

            !state.errorMessage.isNullOrBlank() && routeMap == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
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

            routeMap != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = routeMap.routeName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${routeMap.totalDistanceKm ?: 0.0} км • ${routeMap.totalDurationMin ?: 0} мин",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (routeMap.days.size > 1) {
                        ScrollableTabRow(
                            selectedTabIndex = routeMap.days.indexOfFirst {
                                it.dayNumber == state.selectedDayNumber
                            }.coerceAtLeast(0)
                        ) {
                            routeMap.days.forEach { day ->
                                Tab(
                                    selected = day.dayNumber == state.selectedDayNumber,
                                    onClick = { onDaySelected(day.dayNumber) },
                                    text = { Text("День ${day.dayNumber}") }
                                )
                            }
                        }
                    }

                    YandexRouteMapView(
                        day = selectedDay,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}