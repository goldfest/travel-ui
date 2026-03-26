package com.travelguide.ui.screens.route

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMapScreen(
    route: Route?,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onViewList: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val startColor = MaterialTheme.colorScheme.error
    val finishColor = Color(0xFF2E7D32)
    val middlePointColor = Color(0xFF1976D2)

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
    ) { paddingValues ->
        when {
            isLoading && route == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Загрузка карты…")
                }
            }

            !errorMessage.isNullOrBlank() && route == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text("Повторить")
                        }
                    }
                }
            }

            route != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.LightGray.copy(alpha = 0.15f))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val routePoints = route.points
                        val points = if (routePoints.isEmpty()) {
                            listOf(
                                Offset(size.width * 0.2f, size.height * 0.3f),
                                Offset(size.width * 0.4f, size.height * 0.5f),
                                Offset(size.width * 0.6f, size.height * 0.4f),
                                Offset(size.width * 0.8f, size.height * 0.7f)
                            )
                        } else {
                            routePoints.mapIndexed { index, _ ->
                                Offset(
                                    x = size.width * (0.2f + 0.15f * index.coerceAtMost(4)),
                                    y = size.height * (0.25f + 0.1f * (index % 4))
                                )
                            }
                        }

                        if (points.isNotEmpty()) {
                            val path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }

                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 4f)
                            )

                            points.forEachIndexed { index, point ->
                                val pointColor = when (index) {
                                    0 -> startColor
                                    points.size - 1 -> finishColor
                                    else -> middlePointColor
                                }

                                drawCircle(color = pointColor, radius = 16f, center = point)
                                drawCircle(color = Color.White, radius = 8f, center = point)

                                drawContext.canvas.nativeCanvas.drawText(
                                    "${index + 1}",
                                    point.x,
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

                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Text(
                                route.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${route.distanceKm ?: 0.0} км • ${route.durationMin ?: 0} мин",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    LegendCard(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Легенда карты", fontWeight = FontWeight.Bold)
            Text("🔴 Старт")
            Text("🔵 Точки")
            Text("🟢 Финиш")
        }
    }
}