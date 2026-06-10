package com.travelguide.ui.screens.route

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.RouteDay
import com.travelguide.domain.models.RoutePoint
import com.travelguide.domain.models.RouteStatus
import com.travelguide.route.RouteOptimizationForm

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
    onOptimizeClick: (RouteOptimizationForm) -> Unit,
    onDownloadOfflineClick: () -> Unit,
    onExportPdfClick: () -> Unit,
    onExportGpxClick: () -> Unit,
    onExportJsonClick: () -> Unit,
    onExportOfflineArchiveClick: () -> Unit,
    isOfflineMode: Boolean = false,
    isOfflineAvailable: Boolean = false,
    isOptimizing: Boolean = false,
) {
    var exportMenuExpanded by remember { mutableStateOf(false) }
    var optimizeDialogExpanded by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = route?.name ?: "Маршрут",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick, enabled = route != null) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                    IconButton(onClick = onViewMap, enabled = route != null && route.status != RouteStatus.GRAPH_PREPARING) {
                        Icon(Icons.Default.Map, contentDescription = "Карта")
                    }
                }
            )
        },
        bottomBar = {
            if (route != null && !isOfflineMode) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                    ) {
                        FilledTonalButton(
                            onClick = { optimizeDialogExpanded = true },
                            enabled = route.status != RouteStatus.GRAPH_PREPARING && !isOptimizing
                        ) {
                            if (isOptimizing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Tune, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isOptimizing) "Оптимизация…" else "Оптимизировать")
                        }

                        FilledTonalButton(
                            onClick = onDownloadOfflineClick,
                            enabled = route.status != RouteStatus.GRAPH_PREPARING && !isOptimizing
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Оффлайн")
                        }

                        Box {
                            FilledTonalButton(
                                onClick = { exportMenuExpanded = true },
                                enabled = route.status != RouteStatus.GRAPH_PREPARING && !isOptimizing
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Экспорт")
                            }
                            DropdownMenu(
                                expanded = exportMenuExpanded,
                                onDismissRequest = { exportMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("PDF") },
                                    onClick = {
                                        exportMenuExpanded = false
                                        onExportPdfClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("GPX") },
                                    onClick = {
                                        exportMenuExpanded = false
                                        onExportGpxClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("JSON") },
                                    onClick = {
                                        exportMenuExpanded = false
                                        onExportJsonClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("ZIP оффлайн") },
                                    onClick = {
                                        exportMenuExpanded = false
                                        onExportOfflineArchiveClick()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (route != null && optimizeDialogExpanded) {
            RouteOptimizationDialog(
                route = route,
                onDismiss = { optimizeDialogExpanded = false },
                onConfirm = { form ->
                    optimizeDialogExpanded = false
                    onOptimizeClick(form)
                }
            )
        }


        when {
            isLoading && route == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Загрузка маршрута…")
                }
            }

            !errorMessage.isNullOrBlank() && route == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
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
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isOfflineMode) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.large
                            ) {
                                Text(
                                    text = "Оффлайн-копия маршрута. Изменения можно вносить без интернета — они будут автоматически отправлены на сервер после появления сети.",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    item {
                        RouteHeroSection(
                            route = route,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        )
                    }

                    if (route.status == RouteStatus.GRAPH_PREPARING) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Text(
                                    text = "Маршрут ещё готовится. Обновление страницы теперь выполняется только вручную — нажмите «Повторить» или вернитесь позже.",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    if (isOptimizing) {
                        item {
                            OptimizationProgressCard(
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    item {
                        StatsSection(
                            route = route,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        RouteInfoCard(
                            route = route,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }



                    if (route.days.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "План по дням",
                                subtitle = "${route.days.size} ${pluralDays(route.days.size)}",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }

                        items(route.days) { day ->
                            DayOverviewCard(
                                day = day,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }


                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun OptimizationProgressCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            Column {
                Text(
                    text = "Алгоритм оптимизирует маршрут…",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Рассчитываются порядок точек, время посещения и продолжительность дня.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RouteHeroSection(
    route: Route,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = route.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            if (!route.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = route.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RouteTag(route.transportModeText())
                RouteTag(route.status.label())
                RouteTag(if (route.isOptimized) "Оптимизирован" else "Без оптимизации")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatsSection(
    route: Route,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Статистика",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = route.distanceKm?.let { formatDistance(it) } ?: "-",
                subtitle = "Дистанция",
                icon = { Icon(Icons.Default.Route, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(0.48f)
            )
            StatCard(
                title = route.durationMin?.let { formatMinutesWithHours(it) } ?: "-",
                subtitle = "Время",
                icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(0.48f)
            )
            StatCard(
                title = route.points.size.toString(),
                subtitle = "Точек",
                icon = { Icon(Icons.Default.Place, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(0.48f)
            )
            StatCard(
                title = route.days.size.toString(),
                subtitle = "Дней",
                icon = { Icon(Icons.Default.Map, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(0.48f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            ) {
                Box(
                    modifier = Modifier.padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RouteInfoCard(
    route: Route,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Информация о маршруте",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow("Режим", route.transportModeText())
            InfoRow("Статус", route.status.label())
            InfoRow("Оптимизация", if (route.isOptimized) "Да" else "Нет")

            route.startPoint?.let { InfoRow("Начало", it) }
            route.endPoint?.let { InfoRow("Конец", it) }

            if (route.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Предупреждения",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))

                route.warnings.forEach { warning ->
                    Text(
                        text = "• $warning",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DayOverviewCard(
    day: RouteDay,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "День ${day.dayNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    val pointCount = day.points.size
                    Text(
                        text = "$pointCount ${pluralPoints(pointCount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    formatDayDate(day)?.let { formattedDate ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    calculateDayTotalMinutes(day)?.let { totalMinutes ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Общее время дня: ${formatMinutesWithHours(totalMinutes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${day.points.size}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (!day.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = day.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (day.points.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    day.points.sortedBy { it.orderIndex }.forEach { point ->
                        DayPointRow(point = point)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayPointRow(point: RoutePoint) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = point.orderIndex.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = point.poiName ?: point.poi?.name.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            if (!point.poiAddress.isNullOrBlank()) {
                Text(
                    text = point.poiAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Длительность: ${formatMinutesWithHours(point.estimatedVisitMinutes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Время посещения: ${formatPointVisitRange(point) ?: "не задано"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${point.orderIndex}. ${point.poiName ?: point.poi?.name.orEmpty()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (!point.poiAddress.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = point.poiAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Длительность: ${formatMinutesWithHours(point.estimatedVisitMinutes)}",
                style = MaterialTheme.typography.bodySmall
            )

            formatPointVisitRange(point)?.let { visitRange ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Время посещения: $visitRange",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun RouteTag(text: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun formatMinutesWithHours(minutes: Int): String {
    if (minutes <= 0) return "0 мин"
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours <= 0 -> "$rest мин"
        rest <= 0 -> "$hours ч"
        else -> "$hours ч $rest мин"
    }
}

private fun calculateDayTotalMinutes(day: RouteDay): Int? {
    val points = day.points.sortedBy { it.orderIndex }
    if (points.isEmpty()) return null

    val scheduledStart = points.mapNotNull { timeToMinutes(formatIsoTime(it.plannedArrivalAt)) }.minOrNull()
    val scheduledEnd = points.mapNotNull { timeToMinutes(formatIsoTime(it.plannedDepartureAt)) }.maxOrNull()
    if (scheduledStart != null && scheduledEnd != null && scheduledEnd >= scheduledStart) {
        return scheduledEnd - scheduledStart
    }

    return points.sumOf { it.estimatedVisitMinutes }
}

private fun timeToMinutes(value: String?): Int? {
    if (value.isNullOrBlank()) return null
    val parts = value.split(':')
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: return null
    return hour * 60 + minute
}

private fun formatDistance(distanceKm: Double): String {
    return if (distanceKm % 1.0 == 0.0) {
        "${distanceKm.toInt()} км"
    } else {
        "%.1f км".format(distanceKm)
    }
}

private fun pluralDays(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod10 == 1 && mod100 != 11 -> "день"
        mod10 in 2..4 && mod100 !in 12..14 -> "дня"
        else -> "дней"
    }
}

private fun pluralPoints(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod10 == 1 && mod100 != 11 -> "точка"
        mod10 in 2..4 && mod100 !in 12..14 -> "точки"
        else -> "точек"
    }
}

private fun formatIsoDate(value: String?): String? {
    if (value.isNullOrBlank()) return null
    val datePart = value.substringBefore('T')
    val parts = datePart.split("-")
    if (parts.size != 3) return datePart
    return "${parts[2]}.${parts[1]}.${parts[0]}"
}

private fun formatIsoTime(value: String?): String? {
    if (value.isNullOrBlank()) return null
    return value.substringAfter('T', value).take(5)
}

private fun formatPointVisitRange(point: RoutePoint): String? {
    val start = formatIsoTime(point.plannedArrivalAt)
    val end = formatIsoTime(point.plannedDepartureAt)
    return if (!start.isNullOrBlank() && !end.isNullOrBlank()) "$start — $end" else null
}

private fun formatDayDate(day: RouteDay): String? {
    return formatIsoDate(day.routeDate ?: day.plannedStart ?: day.plannedEnd)
}