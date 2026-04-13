package com.travelguide.ui.screens.route

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteStatus
import com.travelguide.route.RouteListFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteListScreen(
    routes: List<Route>,
    isLoading: Boolean,
    filter: RouteListFilter,
    deletingRouteId: Int?,
    errorMessage: String?,
    routeIdsWithDrafts: Set<Int>,
    showApplyDraftsDialog: Boolean,
    isApplyingDrafts: Boolean,
    snackbarHostState: SnackbarHostState,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onFilterChange: (RouteListFilter) -> Unit,
    onRouteClick: (Int) -> Unit,
    onDeleteRoute: (Int) -> Unit,
    onArchiveRoute: (Int) -> Unit,
    onUnarchiveRoute: (Int) -> Unit,
    onCreateRoute: (() -> Unit)? = null,
    onApplyDrafts: () -> Unit,
    onDismissApplyDraftsDialog: () -> Unit,
) {
    var pendingDeleteRoute by remember { mutableStateOf<Route?>(null) }

    if (showApplyDraftsDialog) {
        AlertDialog(
            onDismissRequest = onDismissApplyDraftsDialog,
            title = { Text("Применить оффлайн-изменения") },
            text = {
                Text(
                    "Интернет-соединение восстановлено. Найдены сохранённые черновики изменений маршрутов. Применить их сейчас?"
                )
            },
            confirmButton = {
                TextButton(onClick = onApplyDrafts, enabled = !isApplyingDrafts) {
                    Text("Применить")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissApplyDraftsDialog, enabled = !isApplyingDrafts) {
                    Text("Позже")
                }
            }
        )
    }

    pendingDeleteRoute?.let { route ->
        AlertDialog(
            onDismissRequest = { pendingDeleteRoute = null },
            title = {
                Text(
                    when (filter) {
                        RouteListFilter.ACTIVE -> "Действия с маршрутом"
                        RouteListFilter.ARCHIVED -> "Архивный маршрут"
                        RouteListFilter.OFFLINE -> "Оффлайн-маршрут"
                    }
                )
            },
            text = {
                Text(
                    when (filter) {
                        RouteListFilter.ACTIVE -> "Маршрут «${route.name}» можно переместить в архив или удалить окончательно."
                        RouteListFilter.ARCHIVED -> "Маршрут «${route.name}» можно вернуть из архива или удалить окончательно."
                        RouteListFilter.OFFLINE -> "Маршрут «${route.name}» будет удалён только с устройства. На сервере он останется без изменений."
                    }
                )
            },
            confirmButton = {
                when (filter) {
                    RouteListFilter.ACTIVE -> TextButton(
                        onClick = {
                            pendingDeleteRoute = null
                            onArchiveRoute(route.id)
                        }
                    ) { Text("В архив") }

                    RouteListFilter.ARCHIVED -> TextButton(
                        onClick = {
                            pendingDeleteRoute = null
                            onUnarchiveRoute(route.id)
                        }
                    ) { Text("Вернуть") }

                    RouteListFilter.OFFLINE -> TextButton(
                        onClick = {
                            pendingDeleteRoute = null
                            onDeleteRoute(route.id)
                        },
                        enabled = deletingRouteId == null
                    ) { Text("Удалить с устройства") }
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (filter != RouteListFilter.OFFLINE) {
                        TextButton(
                            onClick = {
                                pendingDeleteRoute = null
                                onDeleteRoute(route.id)
                            },
                            enabled = deletingRouteId == null
                        ) {
                            Text("Удалить")
                        }
                    }
                    TextButton(onClick = { pendingDeleteRoute = null }) {
                        Text("Отмена")
                    }
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Маршруты") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            if (filter != RouteListFilter.ARCHIVED && onCreateRoute != null) {
                FloatingActionButton(onClick = onCreateRoute) {
                    Icon(Icons.Default.Add, contentDescription = "Создать маршрут")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            RouteFilterBar(
                selected = filter,
                onFilterChange = onFilterChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            when {
                isLoading && routes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Загрузка маршрутов…")
                    }
                }

                !errorMessage.isNullOrBlank() && routes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(errorMessage, color = MaterialTheme.colorScheme.error)
                            Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                                Text("Повторить")
                            }
                        }
                    }
                }

                routes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = if (filter == RouteListFilter.OFFLINE) Icons.Default.DownloadDone else Icons.Default.Route,
                                contentDescription = null
                            )
                            Text(
                                when (filter) {
                                    RouteListFilter.ACTIVE -> "У вас еще нет активных маршрутов"
                                    RouteListFilter.ARCHIVED -> "Архивных маршрутов пока нет"
                                    RouteListFilter.OFFLINE -> "Нет маршрутов, подготовленных для оффлайн-работы"
                                }
                            )
                            if (filter != RouteListFilter.ARCHIVED && onCreateRoute != null) {
                                Button(onClick = onCreateRoute) {
                                    Text("Создать маршрут")
                                }
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(routes, key = { it.id }) { route ->
                            RouteCard(
                                route = route,
                                isDeleting = deletingRouteId == route.id,
                                showOfflineBadge = filter == RouteListFilter.OFFLINE,
                                hasSavedDraft = route.id in routeIdsWithDrafts,
                                onClick = { onRouteClick(route.id) },
                                onLongClick = { pendingDeleteRoute = route }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteFilterBar(
    selected: RouteListFilter,
    onFilterChange: (RouteListFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == RouteListFilter.ACTIVE,
            onClick = { onFilterChange(RouteListFilter.ACTIVE) },
            label = { Text("Активные") }
        )
        FilterChip(
            selected = selected == RouteListFilter.ARCHIVED,
            onClick = { onFilterChange(RouteListFilter.ARCHIVED) },
            label = { Text("Архив") }
        )
        FilterChip(
            selected = selected == RouteListFilter.OFFLINE,
            onClick = { onFilterChange(RouteListFilter.OFFLINE) },
            label = { Text("Оффлайн") }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RouteCard(
    route: Route,
    isDeleting: Boolean,
    showOfflineBadge: Boolean,
    hasSavedDraft: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                Surface { Text(text = route.status.label(), style = MaterialTheme.typography.labelMedium) }
                if (showOfflineBadge) {
                    Surface { Text(text = "Оффлайн", style = MaterialTheme.typography.labelMedium) }
                }
                if (showOfflineBadge && hasSavedDraft) {
                    Surface { Text(text = "Есть черновик", style = MaterialTheme.typography.labelMedium) }
                }
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
    Row(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Удерживайте для удаления",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
