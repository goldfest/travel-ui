// screens/route/RouteDetailScreen.kt
package com.travelguide.ui.screens.route

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.travelguide.domain.models.RouteDay
import com.travelguide.domain.models.RoutePoint
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(
    routeId: Int,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onViewMap: () -> Unit, // Колбэк для просмотра карты
    onViewList: () -> Unit // Колбэк для просмотра списка
) {
    // Используем MockData или создаем временный маршрут
    val route = remember(routeId) {
        // Создаем временный маршрут для демонстрации
        Route(
            id = routeId,
            name = "Историческая Москва",
            description = "Обзор главных достопримечательностей столицы",
            transportMode = "WALK",
            isOptimized = true,
            optimizationMode = "distance",
            distanceKm = 5.2f,
            durationMin = 180,
            startPoint = "Красная площадь",
            endPoint = "Воробьевы горы",
            userId = 1,
            cityId = 1,
            points = listOf(
                RoutePoint(
                    id = 1,
                    orderIndex = 1,
                    poiId = 1,
                    poi = MockData.pois.firstOrNull { it.id == 1 }
                ),
                RoutePoint(
                    id = 2,
                    orderIndex = 2,
                    poiId = 2,
                    poi = MockData.pois.firstOrNull { it.id == 2 }
                ),
                RoutePoint(
                    id = 3,
                    orderIndex = 3,
                    poiId = 3,
                    poi = MockData.pois.firstOrNull { it.id == 3 }
                )
            ),
            days = listOf(
                RouteDay(
                    id = 1,
                    dayNumber = 1,
                    description = "Первый день: центр города",
                    routeId = routeId,
                    points = listOf(
                        RoutePoint(
                            id = 1,
                            orderIndex = 1,
                            poiId = 1,
                            poi = MockData.pois.firstOrNull { it.id == 1 }
                        ),
                        RoutePoint(
                            id = 2,
                            orderIndex = 2,
                            poiId = 2,
                            poi = MockData.pois.firstOrNull { it.id == 2 }
                        )
                    )
                ),
                RouteDay(
                    id = 2,
                    dayNumber = 2,
                    description = "Второй день: парки и музеи",
                    routeId = routeId,
                    points = listOf(
                        RoutePoint(
                            id = 3,
                            orderIndex = 1,
                            poiId = 3,
                            poi = MockData.pois.firstOrNull { it.id == 3 }
                        )
                    )
                )
            )
        )
    }

    var showMapView by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(route.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                    // Кнопка переключения между картой и списком
                    IconButton(onClick = {
                        if (showMapView) {
                            showMapView = false
                        } else {
                            onViewMap()
                        }
                    }) {
                        Icon(
                            if (showMapView) Icons.Default.List else Icons.Default.Map,
                            contentDescription = if (showMapView) "Список" else "Карта"
                        )
                    }
                    IconButton(onClick = { /* TODO: поделиться */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Поделиться")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Кнопка оптимизации
                    FilledTonalButton(onClick = { /* TODO: оптимизировать */ }) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Оптимизировать")
                    }

                    // Кнопка экспорта
                    FilledTonalButton(onClick = { /* TODO: экспорт */ }) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Экспорт")
                    }


                }
            }
        }
    ) { paddingValues ->
        if (showMapView) {
            // Показываем упрощенную карту прямо на этом экране
            SimpleRouteMap(route = route, onBackToList = { showMapView = false })
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Общая информация
                item {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        if (!route.description.isNullOrEmpty()) {
                            Text(
                                text = route.description,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        // Статистика
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = route.distanceKm?.toString() ?: "-",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("км", style = MaterialTheme.typography.labelMedium)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = route.durationMin?.toString() ?: "-",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("минут", style = MaterialTheme.typography.labelMedium)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = route.points.size.toString(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("точек", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        // Детали
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Детали маршрута",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailItem(
                                icon = Icons.Default.Directions,
                                label = "Режим передвижения",
                                value = route.transportModeText()
                            )
                            DetailItem(
                                icon = Icons.Default.Tune,
                                label = "Оптимизация",
                                value = if (route.isOptimized) "Да" else "Нет"
                            )
                            if (route.startPoint != null) {
                                DetailItem(
                                    icon = Icons.Default.Place,
                                    label = "Начало",
                                    value = route.startPoint
                                )
                            }
                            if (route.endPoint != null) {
                                DetailItem(
                                    icon = Icons.Default.Flag,
                                    label = "Конец",
                                    value = route.endPoint
                                )
                            }
                        }
                    }
                }

                // Дни маршрута
                if (route.days.isNotEmpty()) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Дни маршрута",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${route.days.size} дня",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(route.days) { day ->
                        DayCard(day = day, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }

                // Точки маршрута
                if (route.points.isNotEmpty()) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Все точки маршрута (${route.points.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            // Кнопка карты для маленьких экранов
                            IconButton(onClick = { showMapView = true }) {
                                Icon(Icons.Default.Map, contentDescription = "Показать на карте")
                            }
                        }
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

@Composable
private fun SimpleRouteMap(
    route: Route,
    onBackToList: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Заглушка для карты (в реальном приложении здесь будет настоящая карта)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Упрощенная визуализация маршрута
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    // Здесь будет граф с точками и линиями
                    // Временная заглушка
                    Text(
                        text = "Карта маршрута\n\n" +
                                "● Красная площадь\n" +
                                "───→\n" +
                                "● Кремль\n" +
                                "───→\n" +
                                "● Воробьевы горы",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Text(
                    text = "Здесь будет интерактивная карта с маршрутом",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Кнопка возврата к списку
        FloatingActionButton(
            onClick = onBackToList,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.List, contentDescription = "Список")
        }

        // Легенда карты
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .width(200.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Легенда",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                LegendItem(color = Color.Red, text = "Начало маршрута")
                LegendItem(color = Color.Blue, text = "Точки маршрута")
                LegendItem(color = Color.Green, text = "Конец маршрута")
                LegendItem(color = Color.Gray, text = "Линия маршрута")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DayCard(day: RouteDay, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "День ${day.dayNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = "${day.points.size} точек",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (!day.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = day.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (day.points.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    day.points.forEach { point ->
                        RoutePointItem(point = point)
                    }
                }
            }
        }
    }
}

@Composable
fun RoutePointItem(
    point: RoutePoint,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${point.orderIndex}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = point.poi?.name ?: "Точка маршрута",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (!point.poi?.address.isNullOrEmpty()) {
                    Text(
                        text = point.poi?.address ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = { /* TODO: открыть детали */ }) {
                Icon(Icons.Default.Info, contentDescription = "Подробнее")
            }
        }
    }
}