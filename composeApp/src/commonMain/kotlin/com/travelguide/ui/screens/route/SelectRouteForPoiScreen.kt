package com.travelguide.ui.screens.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteDay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SelectRouteForPoiScreen(
    routes: List<Route>,
    isLoading: Boolean,
    errorMessage: String?,
    isAdding: Boolean,
    selectedRouteId: Int?,
    selectedDayNumber: Int?,
    selectedOrderIndex: Int?,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onRouteSelected: (Route) -> Unit,
    onDaySelected: (Route, Int) -> Unit,
    onOrderSelected: (Int) -> Unit,
    onConfirmAdd: (Route) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить в маршрут") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> CenterMessage(paddingValues, "Загрузка маршрутов…")

            !errorMessage.isNullOrBlank() -> {
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

            routes.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Route, contentDescription = null)
                        Spacer(Modifier.height(8.dp))
                        Text("Подходящих маршрутов пока нет")
                        Text(
                            "Сначала создайте маршрут в этом городе",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                    item {
                        Text(
                            text = "Выберите маршрут, день и позицию вставки. Порядок считается с учётом уже добавленных точек.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(routes, key = { it.id }) { route ->
                        val selected = route.id == selectedRouteId
                        RouteChoiceCard(
                            route = route,
                            selected = selected,
                            isAdding = isAdding,
                            selectedDayNumber = selectedDayNumber,
                            selectedOrderIndex = selectedOrderIndex,
                            onRouteSelected = { onRouteSelected(route) },
                            onDaySelected = { dayNumber -> onDaySelected(route, dayNumber) },
                            onOrderSelected = onOrderSelected,
                            onConfirmAdd = { onConfirmAdd(route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CenterMessage(paddingValues: PaddingValues, text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun RouteChoiceCard(
    route: Route,
    selected: Boolean,
    isAdding: Boolean,
    selectedDayNumber: Int?,
    selectedOrderIndex: Int?,
    onRouteSelected: () -> Unit,
    onDaySelected: (Int) -> Unit,
    onOrderSelected: (Int) -> Unit,
    onConfirmAdd: () -> Unit
) {
    val sortedDays = route.days.sortedBy { it.dayNumber }.ifEmpty {
        listOf(RouteDay(id = 0, dayNumber = 1, routeId = route.id))
    }
    val selectedDay = sortedDays.firstOrNull { it.dayNumber == selectedDayNumber } ?: sortedDays.first()
    val positions = buildInsertionPositions(selectedDay)

    ElevatedCard(
        onClick = onRouteSelected,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        route.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!route.description.isNullOrBlank()) {
                        Text(
                            route.description.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = route.status.label(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(route.transportMode.label()) },
                    leadingIcon = { Icon(Icons.Default.Route, contentDescription = null) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("${sortedDays.size} дн.") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("${route.days.sumOf { it.points.size }.ifZero(route.points.size)} точек") }
                )
            }

            if (selected) {
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "1. День маршрута",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sortedDays.forEach { day ->
                        FilterChip(
                            selected = day.dayNumber == selectedDay.dayNumber,
                            onClick = { onDaySelected(day.dayNumber) },
                            label = { Text("День ${day.dayNumber} • ${day.points.size} точек") }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "2. Позиция вставки",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    positions.forEach { option ->
                        FilterChip(
                            selected = selectedOrderIndex == option.index,
                            onClick = { onOrderSelected(option.index) },
                            label = { Text(option.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onRouteSelected,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Изменить")
                    }
                    Button(
                        onClick = onConfirmAdd,
                        enabled = !isAdding && selectedOrderIndex != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isAdding) "Добавляем…" else "Добавить")
                    }
                }
            } else {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Нажмите, чтобы выбрать день и порядок добавления",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class InsertPosition(
    val index: Int,
    val label: String
)

private fun buildInsertionPositions(day: RouteDay): List<InsertPosition> {
    val sorted = day.points.sortedBy { it.orderIndex }
    if (sorted.isEmpty()) return listOf(InsertPosition(1, "В начало дня"))

    val positions = mutableListOf(InsertPosition(1, "В начало"))
    sorted.forEachIndexed { index, point ->
        positions += InsertPosition(
            index = index + 2,
            label = "После ${point.poiName ?: point.poi?.name ?: "точки ${index + 1}"}"
        )
    }
    return positions
}

private fun Int.ifZero(fallback: Int): Int = if (this == 0) fallback else this
