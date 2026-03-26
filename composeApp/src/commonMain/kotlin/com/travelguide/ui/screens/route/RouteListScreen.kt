package com.travelguide.ui.screens.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteListScreen(
    routes: List<Route>,
    isLoading: Boolean,
    showArchived: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onToggleArchived: () -> Unit,
    onRouteClick: (Int) -> Unit,
    onCreateRoute: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showArchived) "Архив маршрутов" else "Мои маршруты") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleArchived) {
                        Icon(Icons.Default.Archive, contentDescription = "Переключить архив")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!showArchived && onCreateRoute != null) {
                FloatingActionButton(onClick = onCreateRoute) {
                    Icon(Icons.Default.Add, contentDescription = "Создать маршрут")
                }
            }
        }
    ) { paddingValues ->
        when {
            isLoading && routes.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Загрузка маршрутов…")
                }
            }

            !errorMessage.isNullOrBlank() && routes.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                        Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                            Text("Повторить")
                        }
                    }
                }
            }

            routes.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Route,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp)
                        )
                        Text(
                            text = if (showArchived) {
                                "Архивных маршрутов пока нет"
                            } else {
                                "У вас еще нет маршрутов"
                            }
                        )
                        if (!showArchived && onCreateRoute != null) {
                            Button(onClick = onCreateRoute) {
                                Text("Создать маршрут")
                            }
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                route.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            if (!route.description.isNullOrBlank()) {
                Text(
                    route.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Surface(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = route.status.label(),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Text(
                text = "${route.transportMode.label()} • ${route.distanceKm ?: 0.0} км • ${route.durationMin ?: 0} мин",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Точек: ${route.points.size}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}