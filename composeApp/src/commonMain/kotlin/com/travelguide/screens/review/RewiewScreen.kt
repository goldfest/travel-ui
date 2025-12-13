// screens/review/ReviewsScreen.kt
package com.travelguide.ui.screens.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelguide.data.mock.MockData
import com.travelguide.domain.models.Review
import com.travelguide.ui.components.RatingBar
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    poiId: Int,
    onBackClick: () -> Unit,
    onWriteReview: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("all") } // all, positive, negative
    var sortBy by remember { mutableStateOf("date") } // date, rating, helpful

    val poi = MockData.pois.firstOrNull { it.id == poiId } ?: MockData.pois.first()
    val reviews = listOf(
        Review(
            id = 1,
            rating = 5,
            comment = "Отличное место! Обязательно к посещению.",
            createdAt = "2024-01-15",
            likesCount = 24,
            poiId = poiId,
            userId = 1,
            user = com.travelguide.domain.models.User(
                id = 1,
                email = "user@example.com",
                username = "Алексей"
            )
        ),
        Review(
            id = 2,
            rating = 4,
            comment = "Интересно, но многолюдно. Приходите рано утром.",
            createdAt = "2024-01-10",
            likesCount = 12,
            poiId = poiId,
            userId = 2,
            user = com.travelguide.domain.models.User(
                id = 2,
                email = "user2@example.com",
                username = "Мария"
            )
        ),
        Review(
            id = 3,
            rating = 2,
            comment = "Переоцененное место. Очереди огромные, не стоило того.",
            createdAt = "2024-01-05",
            likesCount = 3,
            poiId = poiId,
            userId = 3,
            user = com.travelguide.domain.models.User(
                id = 3,
                email = "user3@example.com",
                username = "Иван"
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Отзывы") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onWriteReview,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Написать отзыв")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Статистика отзывов
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "${poi.averageRating ?: 0}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold
                            )
                            RatingBar(rating = poi.averageRating ?: 0f)
                            Text(
                                text = "${poi.ratingCount} отзывов",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Распределение оценок
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (5 downTo 1).forEach { rating ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("$rating", modifier = Modifier.width(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    LinearProgressIndicator(
                                        progress = when(rating) {
                                            5 -> 0.6f
                                            4 -> 0.25f
                                            3 -> 0.1f
                                            2 -> 0.04f
                                            1 -> 0.01f
                                            else -> 0f
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Фильтры и сортировка
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "all",
                    onClick = { selectedFilter = "all" },
                    label = { Text("Все") }
                )
                FilterChip(
                    selected = selectedFilter == "positive",
                    onClick = { selectedFilter = "positive" },
                    label = { Text("Положительные") }
                )
                FilterChip(
                    selected = selectedFilter == "negative",
                    onClick = { selectedFilter = "negative" },
                    label = { Text("Критические") }
                )

                Spacer(modifier = Modifier.weight(1f))

                // Сортировка
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextButton(onClick = { expanded = true }) {
                        Text("Сортировка")
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("По дате") },
                            onClick = { sortBy = "date"; expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("По оценке") },
                            onClick = { sortBy = "rating"; expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("По полезности") },
                            onClick = { sortBy = "helpful"; expanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Список отзывов
            val filteredReviews = when(selectedFilter) {
                "positive" -> reviews.filter { it.rating >= 4 }
                "negative" -> reviews.filter { it.rating <= 2 }
                else -> reviews
            }

            if (filteredReviews.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Comment,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Отзывов пока нет",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Будьте первым, кто оставит отзыв!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onWriteReview) {
                            Text("Написать отзыв")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredReviews) { review ->
                        ReviewCard(review = review)
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewCard(
    review: Review
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                review.rating >= 4 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                review.rating <= 2 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок с пользователем
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Аватар пользователя
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = review.user?.username?.first()?.toString() ?: "U",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = review.user?.username ?: "Пользователь",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatDate(review.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Рейтинг
                RatingBar(rating = review.rating.toFloat())
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Текст отзыва
            if (!review.comment.isNullOrEmpty()) {
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.4
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Изображения отзыва
            if (review.images.isNotEmpty()) {
                // TODO: Галерея изображений
                Text(
                    text = "${review.images.size} фото",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Действия
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { /* TODO: лайк */ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = "Полезно"
                        )
                    }
                    Text(
                        text = review.likesCount.toString(),
                        style = MaterialTheme.typography.labelMedium
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = { /* TODO: жалоба */ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Report,
                            contentDescription = "Пожаловаться"
                        )
                    }
                }

                // Ответ
                TextButton(onClick = { /* TODO: ответить */ }) {
                    Text("Ответить")
                }
            }
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}