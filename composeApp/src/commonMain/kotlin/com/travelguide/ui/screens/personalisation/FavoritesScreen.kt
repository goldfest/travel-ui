package com.travelguide.ui.screens.personalisation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.Favorite
import com.travelguide.domain.models.PoiCardUiModel
import com.travelguide.components.cards.POICard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FavoritesScreen(
    favorites: List<Favorite>,
    isLoading: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onPOIClick: (Int) -> Unit,
    onRemoveFavorite: (Int) -> Unit,
    onRetry: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedCity by remember { mutableStateOf<Int?>(null) }

    val categories = listOf("Все", "Рестораны", "Музеи", "Отели", "Парки")
    val cityOptions = buildList {
        add("Все города")
        favorites.mapNotNull { it.poi?.cityId }
            .distinct()
            .forEach { add("Город $it") }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Избранное") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Еще")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(errorMessage)
                        Spacer(modifier = Modifier.height(12.dp))
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
                        .padding(paddingValues)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
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

                            Text(
                                text = "Города",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                cityOptions.forEachIndexed { index, city ->
                                    val cityId = if (index == 0) null else favorites.mapNotNull { it.poi?.cityId }.distinct().getOrNull(index - 1)

                                    FilterChip(
                                        selected = (selectedCity == null && index == 0) ||
                                                (cityId != null && selectedCity == cityId),
                                        onClick = {
                                            selectedCity = cityId
                                        },
                                        label = { Text(city) }
                                    )
                                }
                            }
                        }
                    }

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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        val filteredFavorites = favorites.filter { favorite ->
                            val poi = favorite.poi ?: return@filter false

                            val categoryMatch = selectedCategory?.let { category ->
                                when (category) {
                                    "Рестораны" -> poi.poiType?.code == "restaurant"
                                    "Музеи" -> poi.tags.contains("museum") || poi.poiType?.name?.contains("музей", true) == true
                                    "Отели" -> poi.poiType?.code == "hotel"
                                    "Парки" -> poi.tags.contains("park") || poi.poiType?.name?.contains("парк", true) == true
                                    else -> true
                                }
                            } ?: true

                            val cityMatch = selectedCity?.let { poi.cityId == it } ?: true

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
                                        item = PoiCardUiModel(
                                            poi = poi,
                                            isFavorite = true
                                        ),
                                        onClick = { onPOIClick(poi.id) },
                                        onFavoriteClick = { onRemoveFavorite(poi.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}