// screens/search/SearchScreen.kt
package com.travelguide.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.travelguide.data.mock.MockData
import com.travelguide.ui.components.cards.POICard
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onPOIClick: (Int) -> Unit
) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var recentSearches by remember { mutableStateOf(listOf("Кремль", "Рестораны", "Музеи")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
            // Поле поиска
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Что вы ищете?") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchText.text.isNotEmpty()) {
                        IconButton(onClick = { searchText = TextFieldValue("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (searchText.text.isEmpty()) {
                // Недавние поиски
                Text(
                    text = "Недавние поиски",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    recentSearches.forEach { query ->
                        Card(
                            onClick = { searchText = TextFieldValue(query) },
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
                                Text(text = query)
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Популярные категории
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Популярные категории",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Рестораны", "Отели", "Музеи", "Парки", "Туалеты", "Магазины").forEach { category ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.clickable { searchText = TextFieldValue(category) }
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            } else {
                // Результаты поиска
                val results = MockData.pois.filter { poi ->
                    poi.name.contains(searchText.text, ignoreCase = true) ||
                            poi.description?.contains(searchText.text, ignoreCase = true) == true ||
                            poi.tags.any { it.contains(searchText.text, ignoreCase = true) }
                }

                Text(
                    text = "Результаты поиска (${results.size})",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(results) { poi ->
                        POICard(
                            poi = poi,
                            onClick = { onPOIClick(poi.id) },
                            onFavoriteClick = { /* TODO */ },
                            isFavorite = false
                        )
                    }
                }
            }
        }
    }
}