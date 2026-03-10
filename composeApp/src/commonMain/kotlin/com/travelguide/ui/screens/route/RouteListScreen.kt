// screens/route/RouteListScreen.kt
package com.travelguide.ui.screens.route

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelguide.data.mock.MockData
import com.travelguide.domain.models.Route
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteListScreen(
    onBackClick: () -> Unit,
    onRouteClick: (Int) -> Unit,
    onCreateRoute: () -> Unit
) {
    var showArchived by remember { mutableStateOf(false) }
    val routes = listOf(
        Route(
            id = 1,
            name = "Историческая Москва",
            description = "Обзор главных достопримечательностей",
            transportMode = "WALK",
            distanceKm = 5.2f,
            durationMin = 180,
            userId = 1,
            cityId = 1
        ),
        Route(
            id = 2,
            name = "Гастрономический тур",
            description = "Лучшие рестораны города",
            transportMode = "MIXED",
            distanceKm = 3.7f,
            durationMin = 240,
            userId = 1,
            cityId = 1
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои маршруты") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showArchived = !showArchived }) {
                        Icon(
                            if (showArchived) Icons.Default.Archive else Icons.Default.Archive,
                            contentDescription = if (showArchived) "Показать активные" else "Показать архивные"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRoute,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать маршрут")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (routes.isEmpty()) {
                // Пустой экран
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Route,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "У вас еще нет маршрутов",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Создайте свой первый маршрут",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onCreateRoute) {
                            Text("Создать маршрут")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(routes) { route ->
                        RouteCard(
                            route = route,
                            onClick = { onRouteClick(route.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RouteCard(
    route: Route,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок и статус
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                if (route.isArchived) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "Архив",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Описание
            if (!route.description.isNullOrEmpty()) {
                Text(
                    text = route.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Информация о маршруте
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Длина
                if (route.distanceKm != null) {
                    RouteInfoItem(
                        icon = Icons.Default.DirectionsWalk,
                        text = "${route.distanceKm} км"
                    )
                }

                // Время
                if (route.durationMin != null) {
                    RouteInfoItem(
                        icon = Icons.Default.Schedule,
                        text = "${route.durationMin} мин"
                    )
                }

                // Транспорт
                RouteInfoItem(
                    icon = Icons.Default.Directions,
                    text = route.transportModeText()
                )
            }

            // Точки маршрута
            if (route.points.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Точки маршрута (${route.points.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    route.points.take(3).forEachIndexed { index, point ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = point.poi?.name ?: "Точка ${index + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                    if (route.points.size > 3) {
                        Text(
                            text = "и еще ${route.points.size - 3} точек...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RouteInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}