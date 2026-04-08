package com.travelguide.ui.screens.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.City
import com.travelguide.domain.models.POI
import com.travelguide.domain.models.TransportMode
import com.travelguide.route.EditableRouteDayUi
import com.travelguide.route.EditableRoutePointUi
import com.travelguide.route.RouteEditorMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteEditorScreen(
    mode: RouteEditorMode,
    availableCities: List<City>,
    selectedCityId: Int?,
    selectedCityName: String,
    isGraphLoading: Boolean,
    isGraphReady: Boolean,
    isGraphDownloadInProgress: Boolean,
    graphMessage: String?,
    routeName: String,
    routeDescription: String,
    selectedTransport: TransportMode,
    searchQuery: String,
    availablePois: List<POI>,
    days: List<EditableRouteDayUi>,
    selectedDayNumber: Int,
    isLoading: Boolean,
    isSaving: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onCitySelected: (Int) -> Unit,
    onDownloadGraphClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTransportChange: (TransportMode) -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectDay: (Int) -> Unit,
    onAddDay: () -> Unit,
    onRemoveDay: (Int) -> Unit,
    onDayDescriptionChange: (Int, String) -> Unit,
    onAddPoiToDay: (POI) -> Unit,
    onRemovePoiFromDay: (Int, Int) -> Unit,
    onMovePoint: (Int, Int, Int) -> Unit,
    onSaveClick: () -> Unit
) {
    val selectedDay = days.firstOrNull { it.dayNumber == selectedDayNumber }
    val saveEnabled = !isSaving && (mode == RouteEditorMode.EDIT || isGraphReady)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (mode == RouteEditorMode.CREATE) "Создание маршрута"
                        else "Редактирование маршрута"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSaveClick,
                        enabled = saveEnabled
                    ) {
                        Text(if (isSaving) "Сохранение..." else "Сохранить")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (mode == RouteEditorMode.CREATE) {
                            CitySelectorCard(
                                availableCities = availableCities,
                                selectedCityId = selectedCityId,
                                selectedCityName = selectedCityName,
                                onCitySelected = onCitySelected
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        } else {
                            OutlinedTextField(
                                value = selectedCityName,
                                onValueChange = {},
                                label = { Text("Город") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (mode == RouteEditorMode.CREATE && selectedCityId != null) {
                            GraphStatusCard(
                                isGraphLoading = isGraphLoading,
                                isGraphReady = isGraphReady,
                                isGraphDownloadInProgress = isGraphDownloadInProgress,
                                graphMessage = graphMessage,
                                onDownloadGraphClick = onDownloadGraphClick
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        OutlinedTextField(
                            value = routeName,
                            onValueChange = onNameChange,
                            label = { Text("Название маршрута*") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = routeDescription,
                            onValueChange = onDescriptionChange,
                            label = { Text("Описание") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Режим передвижения",
                            style = MaterialTheme.typography.labelLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(TransportMode.entries) { modeItem ->
                                FilterChip(
                                    selected = selectedTransport == modeItem,
                                    onClick = { onTransportChange(modeItem) },
                                    label = { Text(modeItem.label()) }
                                )
                            }
                        }
                    }
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                item {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Дни маршрута",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            days.forEach { day ->
                                FilterChip(
                                    selected = day.dayNumber == selectedDayNumber,
                                    onClick = { onSelectDay(day.dayNumber) },
                                    label = { Text("День ${day.dayNumber}") }
                                )
                            }

                            if (mode == RouteEditorMode.CREATE) {
                                FilterChip(
                                    selected = false,
                                    onClick = onAddDay,
                                    label = {
                                        Row {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("День")
                                        }
                                    }
                                )
                            }
                        }

                        if (selectedDay != null) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Описание дня ${selectedDay.dayNumber}",
                                    style = MaterialTheme.typography.labelLarge
                                )

                                if (mode == RouteEditorMode.CREATE && days.size > 1) {
                                    TextButton(onClick = { onRemoveDay(selectedDay.dayNumber) }) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Удалить день")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = selectedDay.description,
                                onValueChange = { onDayDescriptionChange(selectedDay.dayNumber, it) },
                                label = { Text("Описание дня") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )
                        }
                    }
                }
            }

            if (selectedDay != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Точки дня ${selectedDay.dayNumber}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (selectedDay.points.isEmpty()) {
                                Text(
                                    text = "Точек пока нет",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                selectedDay.points.forEachIndexed { index, point ->
                                    EditorRoutePointCard(
                                        index = index,
                                        totalCount = selectedDay.points.size,
                                        point = point,
                                        onMoveUp = {
                                            if (index > 0) {
                                                onMovePoint(selectedDay.dayNumber, index, index - 1)
                                            }
                                        },
                                        onMoveDown = {
                                            if (index < selectedDay.points.lastIndex) {
                                                onMovePoint(selectedDay.dayNumber, index, index + 1)
                                            }
                                        },
                                        onRemove = {
                                            onRemovePoiFromDay(selectedDay.dayNumber, point.poi.id)
                                        }
                                    )

                                    if (index != selectedDay.points.lastIndex) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Добавить точки в день $selectedDayNumber",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            label = { Text("Поиск объектов") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            items(availablePois) { poi ->
                val isSelected = selectedDay?.points?.any { it.poi.id == poi.id } == true

                POIAddCard(
                    poi = poi,
                    isSelected = isSelected,
                    onToggle = {
                        if (!isSelected) {
                            onAddPoiToDay(poi)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun GraphStatusCard(
    isGraphLoading: Boolean,
    isGraphReady: Boolean,
    isGraphDownloadInProgress: Boolean,
    graphMessage: String?,
    onDownloadGraphClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isGraphReady) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Граф дорог города",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = graphMessage ?: if (isGraphReady) {
                    "Граф дорог готов. Можно создавать маршрут."
                } else {
                    "Перед созданием маршрута нужно скачать граф города."
                },
                color = if (isGraphReady) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                }
            )

            if (isGraphLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(12.dp))

            FilledTonalButton(
                onClick = onDownloadGraphClick,
                enabled = !isGraphReady && !isGraphDownloadInProgress
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isGraphDownloadInProgress) "Скачивание..."
                    else if (isGraphReady) "Граф уже скачан"
                    else "Скачать граф"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitySelectorCard(
    availableCities: List<City>,
    selectedCityId: Int?,
    selectedCityName: String,
    onCitySelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = selectedCityName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Город*") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { expanded = true }) {
            Text(if (selectedCityId == null) "Выбрать город" else "Сменить город")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableCities.forEach { city ->
                DropdownMenuItem(
                    text = {
                        Text(listOfNotNull(city.name, city.country).joinToString(", "))
                    },
                    onClick = {
                        expanded = false
                        if (selectedCityId != city.id) {
                            onCitySelected(city.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EditorRoutePointCard(
    index: Int,
    totalCount: Int,
    point: EditableRoutePointUi,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${index + 1}. ${point.poi.name}",
                    style = MaterialTheme.typography.titleMedium
                )

                if (!point.poi.address.isNullOrBlank()) {
                    Text(
                        text = point.poi.address.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Посещение: ${point.estimatedVisitMinutes} мин",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onMoveUp, enabled = index > 0) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Вверх")
                }
                IconButton(onClick = onMoveDown, enabled = index < totalCount - 1) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Вниз")
                }
                TextButton(onClick = onRemove) {
                    Text("Удалить")
                }
            }
        }
    }
}

@Composable
fun POIAddCard(
    poi: POI,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(poi.name, style = MaterialTheme.typography.titleMedium)

            if (!poi.address.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    poi.address.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onToggle,
                enabled = !isSelected
            ) {
                Text(if (isSelected) "Уже добавлено" else "Добавить")
            }
        }
    }
}
