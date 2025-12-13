// screens/route/CreateRouteScreen.kt
package com.travelguide.ui.screens.route

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.travelguide.data.mock.MockData
import com.travelguide.ui.components.cards.POICard
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRouteScreen(
    onBackClick: () -> Unit,
    onSubmit: (Int) -> Unit
) {
    var routeName by remember { mutableStateOf("") }
    var routeDescription by remember { mutableStateOf("") }
    var selectedTransport by remember { mutableStateOf("WALK") }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedPOIs by remember { mutableStateOf<List<Int>>(emptyList()) }

    val transportModes = listOf(
        "WALK" to "Пешком",
        "PUBLIC_TRANSPORT" to "Общественный транспорт",
        "CAR" to "На машине",
        "MIXED" to "Смешанный"
    )

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
                    IconButton(
                        onClick = {
                            // TODO: создать маршрут
                            onSubmit(1) // временный ID
                        },
                        enabled = routeName.isNotEmpty() && selectedPOIs.isNotEmpty()
                    ) {
                        Text("Создать")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Форма создания
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Название маршрута
                    OutlinedTextField(
                        value = routeName,
                        onValueChange = { routeName = it },
                        label = { Text("Название маршрута*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Описание
                    OutlinedTextField(
                        value = routeDescription,
                        onValueChange = { routeDescription = it },
                        label = { Text("Описание") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Режим передвижения
                    Text(
                        text = "Режим передвижения",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        transportModes.forEach { (mode, label) ->
                            FilterChip(
                                selected = selectedTransport == mode,
                                onClick = { selectedTransport = mode },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            // Выбранные точки
            if (selectedPOIs.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Выбранные точки (${selectedPOIs.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(200.dp)
                        ) {
                            items(selectedPOIs) { poiId ->
                                val poi = MockData.pois.firstOrNull { it.id == poiId }
                                poi?.let {
                                    RoutePointCard(
                                        point = com.travelguide.domain.models.RoutePoint(
                                            id = poiId,
                                            orderIndex = selectedPOIs.indexOf(poiId) + 1,
                                            poiId = poiId,
                                            poi = poi
                                        ),
                                        onRemove = {
                                            selectedPOIs = selectedPOIs.filter { it != poiId }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Поиск объектов для добавления
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
                        onValueChange = { searchQuery = it },
                        label = { Text("Поиск объектов") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Список объектов для выбора
            val filteredPOIs = MockData.pois.filter { poi ->
                searchQuery.text.isEmpty() ||
                        poi.name.contains(searchQuery.text, ignoreCase = true)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredPOIs) { poi ->
                    POIAddCard(
                        poi = poi,
                        isSelected = selectedPOIs.contains(poi.id),
                        onToggle = {
                            selectedPOIs = if (selectedPOIs.contains(poi.id)) {
                                selectedPOIs.filter { it != poi.id }
                            } else {
                                selectedPOIs + poi.id
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun POIAddCard(
    poi: com.travelguide.domain.models.POI,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = poi.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = poi.poiType?.name ?: "Объект",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { /* TODO: предпросмотр */ },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = "Подробнее")
            }
        }
    }
}

@Composable
fun RoutePointCard(
    point: com.travelguide.domain.models.RoutePoint,
    onRemove: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = point.poi?.poiType?.name ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}