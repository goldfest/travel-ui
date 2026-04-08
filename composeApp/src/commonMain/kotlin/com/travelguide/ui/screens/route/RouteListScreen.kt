package com.travelguide.ui.screens.route

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteListScreen(
    routes: List<Route>,
    isLoading: Boolean,
    showArchived: Boolean,
    deletingRouteId: Int?,
    errorMessage: String?,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onToggleArchived: () -> Unit,
    onRouteClick: (Int) -> Unit,
    onDeleteRoute: (Int) -> Unit,
    onCreateRoute: (() -> Unit)? = null
) {
    var pendingDeleteRoute by remember { mutableStateOf<Route?>(null) }

    pendingDeleteRoute?.let { route ->
        AlertDialog(
            onDismissRequest = { pendingDeleteRoute = null },
            title = { Text("Удалить маршрут?") },
            text = { Text("Маршрут «${route.name}» будет удалён без возможности восстановления.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteRoute = null
                        onDeleteRoute(route.id)
                    },
                    enabled = deletingRouteId == null
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteRoute = null }) {
                    Text("Отмена")
                }
            }
        )
    }

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
                    items(routes, key = { it.id }) { route ->
                        RouteCard(
                            route = route,
                            isDeleting = deletingRouteId == route.id,
                            onClick = { onRouteClick(route.id) },
                            onLongClick = { pendingDeleteRoute = route }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RouteCard(
    route: Route,
    isDeleting: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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

            if (route.status == RouteStatus.GRAPH_PREPARING) {
                Text(
                    text = "Маршрут строится, подождите",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
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

            if (isDeleting) {
                Text(
                    text = "Удаление...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                RowDeleteHint()
            }
        }
    }
}

@Composable
private fun RowDeleteHint() {
    Surface(
        modifier = Modifier.padding(top = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Text(
                text = "Удерживайте для удаления",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
