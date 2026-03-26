package com.travelguide.ui.screens.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(
    route: Route?,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onViewMap: () -> Unit,
    onViewList: () -> Unit,
    onOptimizeClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(route?.name ?: "Маршрут") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick, enabled = route != null) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                    IconButton(onClick = onViewMap, enabled = route != null) {
                        Icon(Icons.Default.Map, contentDescription = "Карта")
                    }
                    IconButton(onClick = { }, enabled = route != null) {
                        Icon(Icons.Default.Share, contentDescription = "Поделиться")
                    }
                }
            )
        },
        bottomBar = {
            if (route != null) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilledTonalButton(onClick = onOptimizeClick) {
                            Icon(Icons.Default.Tune, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Оптимизировать")
                        }

                        FilledTonalButton(onClick = { }) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Экспорт")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            isLoading && route == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp)
                ) {
                    Text("Загрузка маршрута…")
                }
            }

            !errorMessage.isNullOrBlank() && route == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp)
                ) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                        Text("Повторить")
                    }
                }
            }

            route != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (!route.description.isNullOrBlank()) {
                                Text(
                                    text = route.description,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatColumn(route.distanceKm?.toString() ?: "-", "км")
                                    StatColumn(route.durationMin?.toString() ?: "-", "минут")
                                    StatColumn(route.points.size.toString(), "точек")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Детали маршрута",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Режим: ${route.transportModeText()}")
                            Text("Статус: ${route.status.label()}")
                            Text("Оптимизация: ${if (route.isOptimized) "Да" else "Нет"}")

                            route.startPoint?.let { Text("Начало: $it") }
                            route.endPoint?.let { Text("Конец: $it") }

                            if (route.warnings.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Предупреждения",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                route.warnings.forEach {
                                    Text("• $it", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    if (route.days.isNotEmpty()) {
                        item {
                            Text(
                                text = "Дни маршрута",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(route.days) { day ->
                            DayCard(day = day, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                    }

                    if (route.points.isNotEmpty()) {
                        item {
                            Text(
                                text = "Все точки маршрута (${route.points.size})",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(route.points) { point ->
                            RoutePointItem(
                                point = point,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun DayCard(day: RouteDay, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("День ${day.dayNumber}", style = MaterialTheme.typography.titleMedium)
            if (!day.description.isNullOrBlank()) {
                Text(
                    day.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (day.points.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                day.points.forEach { point ->
                    Text("${point.orderIndex}. ${point.poiName ?: point.poi?.name.orEmpty()}")
                }
            }
        }
    }
}

@Composable
fun RoutePointItem(
    point: com.travelguide.domain.models.RoutePoint,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${point.orderIndex}. ${point.poiName ?: point.poi?.name.orEmpty()}",
                style = MaterialTheme.typography.titleMedium
            )
            if (!point.poiAddress.isNullOrBlank()) {
                Text(
                    point.poiAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "Посещение: ${point.estimatedVisitMinutes} мин",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}