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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.POI
import com.travelguide.domain.models.RoutePoint
import com.travelguide.domain.models.TransportMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRouteScreen(
    routeName: String,
    routeDescription: String,
    selectedTransport: TransportMode,
    searchQuery: String,
    availablePois: List<POI>,
    selectedPois: List<POI>,
    isLoading: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTransportChange: (TransportMode) -> Unit,
    onSearchChange: (String) -> Unit,
    onTogglePoi: (POI) -> Unit,
    onRemovePoi: (Int) -> Unit,
    onSubmitClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создание маршрута") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSubmitClick,
                        enabled = !isLoading && routeName.isNotBlank() && selectedPois.isNotEmpty()
                    ) {
                        Text("Создать")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (isLoading && availablePois.isEmpty()) {
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TransportMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = selectedTransport == mode,
                                    onClick = { onTransportChange(mode) },
                                    label = { Text(mode.label()) }
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

            if (selectedPois.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Выбранные точки (${selectedPois.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.height(220.dp)
                            ) {
                                items(selectedPois) { poi ->
                                    RoutePointCard(
                                        point = RoutePoint(
                                            id = poi.id,
                                            orderIndex = selectedPois.indexOfFirst { it.id == poi.id } + 1,
                                            poiId = poi.id,
                                            poi = poi
                                        ),
                                        onRemove = { onRemovePoi(poi.id) }
                                    )
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
                            text = "Добавить точки в маршрут",
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
                POIAddCard(
                    poi = poi,
                    isSelected = selectedPois.any { it.id == poi.id },
                    onToggle = { onTogglePoi(poi) }
                )
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
                    poi.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onToggle) {
                Text(if (isSelected) "Убрать" else "Добавить")
            }
        }
    }
}

@Composable
fun RoutePointCard(
    point: RoutePoint,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("${point.orderIndex}. ${point.poiName ?: point.poi?.name.orEmpty()}")
                if (!point.poiAddress.isNullOrBlank()) {
                    Text(
                        point.poiAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = onRemove) {
                Text("Удалить")
            }
        }
    }
}