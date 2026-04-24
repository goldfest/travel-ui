package com.travelguide.ui.screens.city

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelguide.city.CityPoiMapUiState
import com.travelguide.city.CityPoiMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityPoiMapScreen(
    state: CityPoiMapUiState,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onTypeSelected: (String?) -> Unit,
    onOpenPoi: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    val selectedTypeName = state.poiTypes
        .firstOrNull { it.code == state.selectedTypeCode }
        ?.name ?: "Все типы"

    val filtered = state.pois
        .filter { poi ->
            state.selectedTypeCode == null || poi.poiType?.code == state.selectedTypeCode
        }
        .filter { poi ->
            searchQuery.isBlank() ||
                    poi.name.contains(searchQuery, ignoreCase = true) ||
                    (poi.description?.contains(searchQuery, ignoreCase = true) == true) ||
                    poi.tags.any { it.contains(searchQuery, ignoreCase = true) }
        }
        .filter { poi ->
            poi.latitude != null && poi.longitude != null
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.city?.name?.let { "Карта: $it" } ?: "Карта объектов") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Загрузка карты…")
                }
            }

            !state.errorMessage.isNullOrBlank() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = onRetry) {
                            Text("Повторить")
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Поиск объектов") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    )

                    ExposedDropdownMenuBox(
                        expanded = typeMenuExpanded,
                        onExpandedChange = { typeMenuExpanded = !typeMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedTypeName,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            label = { Text("Тип объекта") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = typeMenuExpanded,
                            onDismissRequest = { typeMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Все типы") },
                                onClick = {
                                    typeMenuExpanded = false
                                    onTypeSelected(null)
                                }
                            )

                            state.poiTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        typeMenuExpanded = false
                                        onTypeSelected(type.code)
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = "Найдено объектов: ${filtered.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = MaterialTheme.shapes.extraLarge,
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CityPoiMapView(
                                pois = filtered,
                                modifier = Modifier.fillMaxSize(),
                                onPoiClick = onOpenPoi
                            )
                        }
                    }
                }
            }
        }
    }
}