// screens/poi/POIListScreen.kt
package com.travelguide.ui.screens.poi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelguide.data.mock.MockData
import com.travelguide.domain.models.POI
import com.travelguide.ui.components.cards.POICard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POIListScreen(
    cityId: Int,
    onPOIClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    var selectedType by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isFavoriteFilter by remember { mutableStateOf(false) }

    val filteredPOIs = MockData.pois.filter { poi ->
        poi.cityId == cityId &&
                (selectedType == null || poi.poiType?.code == selectedType) &&
                (searchQuery.isEmpty() || poi.name.contains(searchQuery, ignoreCase = true) ||
                        poi.description?.contains(searchQuery, ignoreCase = true) == true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Достопримечательности") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Кнопка фильтров
                    IconButton(onClick = onFilterClick) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтры")
                    }

                    // Кнопка сортировки
                    IconButton(onClick = { /* TODO: сортировка */ }) {
                        Icon(Icons.Default.Tune, contentDescription = "Сортировка")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Поиск
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск объектов") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Быстрые фильтры по типам
                ScrollableTabRow(
                    selectedTabIndex = when(selectedType) {
                        "attraction" -> 0
                        "restaurant" -> 1
                        "hotel" -> 2
                        "toilet" -> 3
                        else -> 4
                    }
                ) {
                    Tab(
                        selected = selectedType == null,
                        onClick = { selectedType = null }
                    ) {
                        Text("Все", modifier = Modifier.padding(8.dp))
                    }

                    Tab(
                        selected = selectedType == "attraction",
                        onClick = { selectedType = "attraction" }
                    ) {
                        Text("Достопримечательности", modifier = Modifier.padding(8.dp))
                    }

                    Tab(
                        selected = selectedType == "restaurant",
                        onClick = { selectedType = "restaurant" }
                    ) {
                        Text("Рестораны", modifier = Modifier.padding(8.dp))
                    }

                    Tab(
                        selected = selectedType == "hotel",
                        onClick = { selectedType = "hotel" }
                    ) {
                        Text("Отели", modifier = Modifier.padding(8.dp))
                    }

                    Tab(
                        selected = selectedType == "toilet",
                        onClick = { selectedType = "toilet" }
                    ) {
                        Text("Туалеты", modifier = Modifier.padding(8.dp))
                    }
                }

                // Список объектов
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPOIs) { poi ->
                        POICard(
                            poi = poi,
                            onClick = { onPOIClick(poi.id) },
                            onFavoriteClick = { isFavorite ->
                                // TODO: Сохранить в избранное
                            },
                            isFavorite = isFavoriteFilter // Временно
                        )
                    }
                }
            }
        }
    )
}