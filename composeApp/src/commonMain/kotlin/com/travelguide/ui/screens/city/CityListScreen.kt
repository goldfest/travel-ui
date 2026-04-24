package com.travelguide.ui.screens.city

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.City
import com.travelguide.theme.TravelAccent
import com.travelguide.theme.TravelAccentDeep
import com.travelguide.theme.TravelAccentSoft
import com.travelguide.theme.TravelDark
import com.travelguide.theme.TravelDanger
import com.travelguide.theme.TravelGlow
import com.travelguide.theme.TravelPanel
import com.travelguide.theme.TravelPanelSoft
import com.travelguide.theme.TravelTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityListScreen(
    cities: List<City>,
    popularCities: List<City>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onSearch: (String) -> Unit,
    onSearchClick: () -> Unit,
    onCityClick: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    isAdmin: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = TravelDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TravelPanelSoft,
                            modifier = Modifier.border(1.dp, TravelGlow, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = TravelAccent,
                                modifier = Modifier.padding(9.dp)
                            )
                        }
                        Column {
                            Text(
                                "Nature Explorer",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Города и маршруты",
                                style = MaterialTheme.typography.bodySmall,
                                color = TravelTextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Поиск", tint = TravelAccent)
                    }
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "Уведомления", tint = Color.White)
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Профиль", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TravelDark)
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { },
                    containerColor = TravelAccent,
                    contentColor = TravelDark
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить город")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(TravelDark),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HeroTravelCard()
                    SearchField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onSearch(it)
                        }
                    )
                }
            }

            item {
                DarkCarouselPanel(
                    title = "Популярные направления",
                    subtitle = "Лучшие города для ближайшей поездки"
                ) {
                    when {
                        isLoading && popularCities.isEmpty() -> CenterState { CircularProgressIndicator(color = TravelAccent) }
                        popularCities.isEmpty() -> EmptyState(text = "Популярные направления пока не найдены")
                        else -> {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(18.dp)
                            ) {
                                items(popularCities) { city ->
                                    FeaturedCityCard(city = city, onClick = { onCityClick(city.id) })
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    title = "Все города",
                    subtitle = "Выбери место для следующего маршрута"
                )
            }

            when {
                isLoading && cities.isEmpty() -> item { CenterState { CircularProgressIndicator(color = TravelAccent) } }

                !errorMessage.isNullOrBlank() -> {
                    item {
                        Card(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = TravelPanel)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Ошибка загрузки", style = MaterialTheme.typography.titleMedium, color = Color.White)
                                Text(errorMessage, color = TravelDanger)
                                Button(
                                    onClick = onRetry,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TravelAccent,
                                        contentColor = TravelDark
                                    )
                                ) { Text("Повторить") }
                            }
                        }
                    }
                }

                cities.isEmpty() -> item { EmptyState(text = "Города не найдены") }

                else -> {
                    items(cities) { city ->
                        NatureCityCard(
                            city = city,
                            onClick = { onCityClick(city.id) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroTravelCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(158.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        TravelAccentSoft,
                        TravelAccentDeep,
                        Color(0xFF101A14)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(34.dp))
            .padding(horizontal = 22.dp, vertical = 22.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(92.dp)
                .clip(CircleShape)
                .background(TravelGlow)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.Black.copy(alpha = 0.22f),
                modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(999.dp))
            ) {
                Text(
                    text = "Планируй поездку",
                    style = MaterialTheme.typography.labelMedium,
                    color = TravelAccentSoft,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Text(
                text = "Куда отправимся?",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Города, маршруты и места для путешествий в одном стиле.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.88f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TravelAccent) },
        placeholder = { Text("Найти город", color = TravelTextSecondary) },
        singleLine = true,
        shape = RoundedCornerShape(999.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = TravelPanel,
            unfocusedContainerColor = TravelPanel,
            focusedBorderColor = TravelAccent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = TravelAccent
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DarkCarouselPanel(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(36.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        TravelPanelSoft,
                        TravelPanel
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(36.dp))
            .padding(vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.82f))
            }
        }
        content()
    }
}

@Composable
private fun SectionTitle(modifier: Modifier = Modifier, title: String, subtitle: String) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TravelTextSecondary)
    }
}

@Composable
private fun FeaturedCityCard(city: City, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(292.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CityPhotoPlaceholder(
            city = city,
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            if (city.isPopular) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.40f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = TravelAccent,
                        modifier = Modifier.padding(10.dp).size(20.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.height(106.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                city.name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = city.country?.let { "Город • $it" } ?: "Город",
                style = MaterialTheme.typography.bodyLarge,
                color = TravelTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = city.description?.takeIf { it.isNotBlank() }
                    ?: "Живописное направление для прогулок, маршрутов и новых впечатлений.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f),
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NatureCityCard(city: City, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CityPhotoPlaceholder(
            city = city,
            modifier = Modifier.size(width = 118.dp, height = 92.dp)
        )

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    city.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (city.isPopular) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = TravelAccent, modifier = Modifier.size(16.dp))
                }
            }
            city.country?.let {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TravelAccent, modifier = Modifier.size(16.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = TravelTextSecondary)
                }
            }
            Text(
                text = city.description?.takeIf { it.isNotBlank() }
                    ?: "Маршруты, достопримечательности и прогулки рядом.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.82f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CityPhotoPlaceholder(
    city: City,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    val colors = remember(city.id) {
        val palettes = listOf(
            listOf(TravelAccentSoft, Color(0xFF4E7667), Color(0xFF172019)),
            listOf(Color(0xFFE8B36D), Color(0xFF9D5F2C), Color(0xFF21130B)),
            listOf(Color(0xFF7DA1C6), Color(0xFF38506D), Color(0xFF10131A)),
            listOf(Color(0xFFB7B48A), Color(0xFF59633F), Color(0xFF15180F)),
            listOf(Color(0xFFCBA6A6), Color(0xFF6B4A5A), Color(0xFF181014))
        )
        palettes[((city.id % palettes.size) + palettes.size) % palettes.size]
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.linearGradient(colors))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(30.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = city.name.take(2).uppercase(),
            color = Color.White.copy(alpha = 0.72f),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.28f)
                        )
                    )
                )
        )
        overlay()
    }
}

@Composable
private fun CenterState(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(TravelPanel)
    ) {
        CenterState {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = TravelTextSecondary)
        }
    }
}
