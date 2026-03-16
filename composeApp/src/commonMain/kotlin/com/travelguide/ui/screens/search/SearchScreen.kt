@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.travelguide.ui.screens.search

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.City
import com.travelguide.domain.models.PoiCardUiModel
import com.travelguide.ui.components.cards.POICard

@Composable
fun SearchScreen(
    query: String,
    cities: List<City>,
    items: List<PoiCardUiModel>,
    selectedCity: City?,
    recentSearches: List<String>,
    isLoading: Boolean,
    errorMessage: String?,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSelectRecentQuery: (String) -> Unit,
    onClearHistory: () -> Unit,
    onSelectCity: (City) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onBackClick: () -> Unit,
    onPOIClick: (Int) -> Unit
) {
    val popularCategories = listOf("Рестораны", "Отели", "Музеи", "Парки", "Туалеты")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Что вы ищете?") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClearQuery) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.padding(8.dp))

            when {
                query.isEmpty() -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Недавние поиски",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (recentSearches.isNotEmpty()) {
                            TextButton(onClick = onClearHistory) {
                                Icon(
                                    Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.padding(2.dp))
                                Text("Очистить")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.padding(4.dp))

                    if (recentSearches.isEmpty()) {
                        Text(
                            text = "История поиска пока пуста",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column {
                            recentSearches.forEach { recent ->
                                Card(
                                    onClick = { onSelectRecentQuery(recent) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = recent)
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.padding(8.dp))

                    Text(
                        text = "Популярные категории",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.padding(4.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        popularCategories.forEach { category ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.clickable { onSelectRecentQuery(category) }
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(errorMessage)
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        item {
                            Text(
                                text = "Города (${cities.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        if (cities.isEmpty()) {
                            item {
                                Text(
                                    text = "Города не найдены",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            items(cities) { city ->
                                Card(
                                    onClick = {
                                        onSelectCity(city)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = null
                                            )
                                            Text(
                                                text = city.name,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }

                                        if (!city.country.isNullOrEmpty()) {
                                            Spacer(modifier = Modifier.padding(2.dp))
                                            Text(
                                                text = city.country,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (!city.description.isNullOrEmpty()) {
                                            Spacer(modifier = Modifier.padding(2.dp))
                                            Text(
                                                text = city.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.padding(4.dp))
                        }

                        item {
                            Text(
                                text = if (selectedCity != null) {
                                    "Объекты в ${selectedCity.name} (${items.size})"
                                } else {
                                    "Объекты"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        if (selectedCity == null) {
                            item {
                                Text(
                                    text = "Выберите город из результатов, чтобы искать достопримечательности внутри него",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (items.isEmpty()) {
                            item {
                                Text(
                                    text = "Объекты не найдены",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            items(items) { item ->
                                POICard(
                                    item = item,
                                    onClick = { onPOIClick(item.poi.id) },
                                    onFavoriteClick = { onToggleFavorite(item.poi.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}