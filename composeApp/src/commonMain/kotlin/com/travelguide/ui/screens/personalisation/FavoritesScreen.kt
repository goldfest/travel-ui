// screens/personalisation/FavoritesScreen.kt
package com.travelguide.ui.screens.personalisation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelguide.data.mock.MockData
import com.travelguide.ui.components.cards.POICard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    onPOIClick: (Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedCity by remember { mutableStateOf<Int?>(null) }

    // Мок данные для избранного
    val favorites = listOf(
        com.travelguide.domain.models.Favorite(
            id = 1,
            poiId = 1,
            userId = 1,
            poi = MockData.pois.firstOrNull { it.id == 1 }
        ),
        com.travelguide.domain.models.Favorite(
            id = 2,
            poiId = 2,
            userId = 1,
            poi = MockData.pois.firstOrNull { it.id == 2 }
        )
    )

    val categories = listOf("Все", "Рестораны", "Музеи", "Отели", "Парки")
    val cities = listOf("Все города", "Москва", "Санкт-Петербург", "Казань")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Избранное") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: настройки */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Еще")
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
            // Фильтры
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Категории
                    Text(
                        text = "Категории",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCategory ?: "Все"),
                        edgePadding = 0.dp
                    ) {
                        categories.forEach { category ->
                            Tab(
                                selected = (selectedCategory ?: "Все") == category,
                                onClick = {
                                    selectedCategory = if (category == "Все") null else category
                                }
                            ) {
                                Text(category, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Города
                    Text(
                        text = "Города",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        cities.forEach { city ->
                            FilterChip(
                                selected = (selectedCity == null && city == "Все города") ||
                                        (city != "Все города" && selectedCity == cities.indexOf(city)),
                                onClick = {
                                    selectedCity = if (city == "Все города") null else cities.indexOf(city)
                                },
                                label = { Text(city) }
                            )
                        }
                    }
                }
            }

            // Список избранного
            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "В избранном пока ничего нет",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Добавляйте понравившиеся места в избранное",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Button(onClick = { /* TODO: перейти к поиску */ }) {
                            Text("Найти интересные места")
                        }
                    }
                }
            } else {
                // Фильтрация
                val filteredFavorites = favorites.filter { favorite ->
                    val poi = favorite.poi
                    if (poi == null) return@filter false

                    val categoryMatch = selectedCategory?.let { category ->
                        when(category) {
                            "Рестораны" -> poi.poiType?.code == "restaurant"
                            "Музеи" -> poi.tags.contains("museum") || poi.poiType?.name?.contains("музей") == true
                            "Отели" -> poi.poiType?.code == "hotel"
                            "Парки" -> poi.tags.contains("park")
                            else -> true
                        }
                    } ?: true

                    val cityMatch = selectedCity?.let { cityId ->
                        poi.cityId == cityId
                    } ?: true

                    categoryMatch && cityMatch
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredFavorites) { favorite ->
                        favorite.poi?.let { poi ->
                            POICard(
                                poi = poi,
                                onClick = { onPOIClick(poi.id) },
                                onFavoriteClick = { /* TODO: удалить из избранного */ },
                                isFavorite = true
                            )
                        }
                    }
                }
            }
        }
    }
}