@file:OptIn(ExperimentalMaterial3Api::class)

package com.travelguide.ui.screens.poi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.POI
import com.travelguide.domain.models.POIType
import com.travelguide.ui.components.cards.POICard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POIListScreen(
    cityId: Int,
    pois: List<POI>,
    poiTypes: List<POIType>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onSearch: (String, String?) -> Unit,
    onPOIClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    var selectedType by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isFavoriteFilter by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery, selectedType) {
        onSearch(searchQuery, selectedType)
    }

    val selectedTabIndex = buildList {
        add("all")
        addAll(poiTypes.map { it.code })
    }.indexOf(
        selectedType ?: "all"
    ).let { if (it < 0) 0 else it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Достопримечательности") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onFilterClick) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтры")
                    }

                    IconButton(onClick = { }) {
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск объектов") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex
                ) {
                    Tab(
                        selected = selectedType == null,
                        onClick = { selectedType = null }
                    ) {
                        Text("Все", modifier = Modifier.padding(8.dp))
                    }

                    poiTypes.forEach { type ->
                        Tab(
                            selected = selectedType == type.code,
                            onClick = { selectedType = type.code }
                        ) {
                            Text(
                                text = type.name,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                when {
                    isLoading && pois.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Ошибка загрузки",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(errorMessage)
                                    Button(onClick = onRetry) {
                                        Text("Повторить")
                                    }
                                }
                            }
                        }
                    }

                    pois.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Объекты не найдены")
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pois) { poi ->
                                POICard(
                                    poi = poi,
                                    onClick = { onPOIClick(poi.id) },
                                    onFavoriteClick = { },
                                    isFavorite = isFavoriteFilter
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}