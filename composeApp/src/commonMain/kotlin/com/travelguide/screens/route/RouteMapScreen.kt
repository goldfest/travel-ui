// screens/route/RouteMapScreen.kt
package com.travelguide.ui.screens.route

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelguide.data.mock.MockData
import com.travelguide.domain.models.Route
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.runtime.remember
import androidx.compose.material3.MaterialTheme.colorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMapScreen(
    routeId: Int,
    onBackClick: () -> Unit,
    onViewList: () -> Unit
) {
    // Создаем маршрут на основе ID
    val route = remember(routeId) {
        Route(
            id = routeId,
            name = "Маршрут $routeId",
            description = "Описание маршрута",
            transportMode = "WALK",
            isOptimized = true,
            optimizationMode = "distance",
            distanceKm = 5.2f,
            durationMin = 180,
            startPoint = "Начальная точка",
            endPoint = "Конечная точка",
            userId = 1,
            cityId = 1,
            points = listOf() // Можно добавить точки маршрута
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Карта маршрута",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Карта (заглушка)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                // Сетка карты
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Рисуем сетку
                    for (x in 0..10) {
                        for (y in 0..10) {
                            val xPos = size.width * x / 10f
                            val yPos = size.height * y / 10f
                            drawCircle(
                                color = Color.Gray.copy(alpha = 0.3f),
                                radius = 2f,
                                center = Offset(xPos, yPos)
                            )
                        }
                    }
                }

                // Граф маршрута
                RouteGraph(route = route)

                // Название маршрута поверх карты
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = route.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${route.distanceKm ?: 0} км • ${route.durationMin ?: 0} мин",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Легенда
            LegendCard(modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp))

            // Контролы масштаба (заглушка)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ControlButton(
                    text = "+",
                    onClick = { /* TODO: увеличение */ }
                )
                ControlButton(
                    text = "-",
                    onClick = { /* TODO: уменьшение */ }
                )
            }
        }
    }
}

@Composable
private fun RouteGraph(route: Route) {
    val colorScheme = MaterialTheme.colorScheme
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Координаты точек
        val points = listOf(
            Offset(size.width * 0.2f, size.height * 0.3f),
            Offset(size.width * 0.4f, size.height * 0.5f),
            Offset(size.width * 0.6f, size.height * 0.4f),
            Offset(size.width * 0.8f, size.height * 0.7f)
        )

        // Рисуем линии маршрута
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            color = colorScheme.primary,
            style = Stroke(width = 4f)
        )

        // Рисуем точки
        points.forEachIndexed { index, point ->
            val pointColor = when (index) {
                0 -> Color.Red
                points.size - 1 -> Color.Green
                else -> Color.Blue
            }

            drawCircle(
                color = pointColor,
                radius = 16f,
                center = point
            )

            drawCircle(
                color = Color.White,
                radius = 8f,
                center = point
            )

            drawContext.canvas.nativeCanvas.drawText(
                "${index + 1}",
                point.x - 4f,
                point.y + 4f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 14f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }

    }
}

@Composable
private fun LegendCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Легенда карты",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            LegendItem(
                color = Color.Red,
                text = "Старт",
                subtext = "Начало маршрута"
            )
            LegendItem(
                color = Color.Blue,
                text = "Точки",
                subtext = "Промежуточные точки"
            )
            LegendItem(
                color = Color.Green,
                text = "Финиш",
                subtext = "Конец маршрута"
            )
            LegendItem(
                color = MaterialTheme.colorScheme.primary,
                text = "Маршрут",
                subtext = "Путь следования"
            )

            Divider()

            Text(
                text = "Всего точек: 4",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String, subtext: String? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            if (subtext != null) {
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ControlButton(text: String, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}