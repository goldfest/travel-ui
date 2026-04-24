@file:OptIn(ExperimentalMaterial3Api::class)

package com.travelguide.ui.screens.city

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.travelguide.components.cards.POICard
import com.travelguide.domain.models.City
import com.travelguide.domain.models.POIType
import com.travelguide.domain.models.PoiCardUiModel

@Composable
fun CityInfoScreen(
    city: City?,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    items: List<PoiCardUiModel>,
    poiTypes: List<POIType>,
    isPoisLoading: Boolean,
    poisErrorMessage: String?,
    onRetryPois: () -> Unit,
    onPOIClick: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onBackClick: () -> Unit,
    onOpenCityMap: () -> Unit,
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = items.filter { item ->
        val poi = item.poi
        (selectedCategory == null || poi.poiType?.code == selectedCategory) &&
                (searchQuery.isBlank() ||
                        poi.name.contains(searchQuery, ignoreCase = true) ||
                        poi.description?.contains(searchQuery, ignoreCase = true) == true ||
                        poi.tags.any { it.contains(searchQuery, ignoreCase = true) })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(Color.Transparent)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> item { CenterLoader() }
                errorMessage != null -> item { ErrorCard("Ошибка загрузки города", errorMessage.orEmpty(), onRetry) }
                city == null -> item { EmptyText("Город не найден") }
                else -> {
                    item {
                        CityHero(
                            city = city,
                            objectCount = items.size,
                            poiPreviewImages = items.mapNotNull { it.poi.images.firstOrNull() },
                            onOpenCityMap = onOpenCityMap
                        )
                    }

                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!city.description.isNullOrBlank()) {
                                Text(
                                    text = city.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 21.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = onOpenCityMap,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Открыть карту объектов города")
                            }

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Поиск в ${city.name}") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            )

                            if (poiTypes.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(poiTypes) { category ->
                                        CategoryChip(
                                            category = category,
                                            isSelected = selectedCategory == category.code,
                                            onClick = {
                                                selectedCategory = if (selectedCategory == category.code) null else category.code
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Объекты",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${filteredItems.size} шт",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    when {
                        isPoisLoading && items.isEmpty() -> item { CenterLoader() }
                        poisErrorMessage != null -> item { ErrorCard("Ошибка загрузки достопримечательностей", poisErrorMessage.orEmpty(), onRetryPois) }
                        filteredItems.isEmpty() -> item { EmptyPois() }
                        else -> {
                            items(filteredItems) { item ->
                                POICard(
                                    item = item,
                                    onClick = { onPOIClick(item.poi.id) },
                                    onFavoriteClick = { onToggleFavorite(item.poi.id) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CityHero(
    city: City,
    objectCount: Int,
    poiPreviewImages: List<String>,
    onOpenCityMap: () -> Unit
) {
    val heroImages = remember(city.id, poiPreviewImages) {
        buildList {
            add(cityHeroUrl(city))
            addAll(poiPreviewImages.filter { it.isNotBlank() })
        }.distinct().take(6)
    }
    var selectedImageIndex by remember(city.id) { mutableStateOf(0) }
    val selectedImage = heroImages.getOrNull(selectedImageIndex) ?: cityHeroUrl(city)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(430.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(selectedImage)
                .crossfade(true)
                .build(),
            contentDescription = city.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = 1050f
                    )
                )
        )

        IconButton(
            onClick = onOpenCityMap,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 16.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
        ) {
            Icon(Icons.Default.Map, contentDescription = "Карта", tint = Color.White)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            HeroImageSwitcher(
                images = heroImages,
                selectedIndex = selectedImageIndex,
                onImageClick = { selectedImageIndex = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = city.name,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )

            if (!city.country.isNullOrBlank()) {
                Text(
                    text = city.country,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.72f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (city.isPopular) {
                    InfoPill(icon = { Icon(Icons.Default.Star, null, Modifier.size(15.dp)) }, text = "Популярный")
                }
                InfoPill(icon = { Icon(Icons.Default.Map, null, Modifier.size(15.dp)) }, text = "$objectCount объектов")
            }
        }
    }
}

@Composable
private fun HeroImageSwitcher(
    images: List<String>,
    selectedIndex: Int,
    onImageClick: (Int) -> Unit
) {
    val visible = images.take(4)
    val hiddenCount = (images.size - visible.size).coerceAtLeast(0)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.42f))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        visible.forEachIndexed { index, url ->
            Box(
                modifier = Modifier
                    .size(width = 58.dp, height = 48.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .clickable { onImageClick(index) }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            when {
                                hiddenCount > 0 && index == visible.lastIndex -> Color.Black.copy(alpha = 0.45f)
                                selectedIndex == index -> Color.White.copy(alpha = 0.18f)
                                else -> Color.Transparent
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (hiddenCount > 0 && index == visible.lastIndex) {
                        Text("+$hiddenCount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPill(icon: @Composable () -> Unit, text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) { icon() }
            Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun cityHeroUrl(city: City): String {
    val key = city.slug?.lowercase() ?: city.name.lowercase()
    return when {
        key.contains("ульянов") || key.contains("ulyanov") -> "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?q=80&w=1400&auto=format&fit=crop"
        key.contains("моск") || key.contains("moscow") -> "https://images.unsplash.com/photo-1513326738677-b964603b136d?q=80&w=1400&auto=format&fit=crop"
        key.contains("санкт") || key.contains("petersburg") -> "https://images.unsplash.com/photo-1556610961-2fecc5927173?q=80&w=1400&auto=format&fit=crop"
        key.contains("казан") || key.contains("kazan") -> "https://images.unsplash.com/photo-1603201236596-eb1a63eb0ede?q=80&w=1400&auto=format&fit=crop"
        else -> "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?q=80&w=1400&auto=format&fit=crop"
    }
}

@Composable
private fun CenterLoader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator() }
}

@Composable
private fun ErrorCard(title: String, message: String, onRetry: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Повторить") }
        }
    }
}

@Composable
private fun EmptyText(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun EmptyPois() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(
                Icons.Default.LocationOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = "Объекты не найдены", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Попробуйте изменить фильтры или поисковый запрос",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: POIType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(category.icon)
                Text(text = category.name, style = MaterialTheme.typography.labelLarge)
            }
        },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
