package com.travelguide.ui.screens.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.Review
import com.travelguide.ui.components.RatingBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    averageRating: Double,
    totalReviews: Long,
    reviews: List<Review>,
    isLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit,
    onWriteReview: () -> Unit,
    onToggleLike: (Int) -> Unit,
    onReportReview: (Int) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("all") }

    val filteredReviews = when (selectedFilter) {
        "positive" -> reviews.filter { it.rating >= 4 }
        "negative" -> reviews.filter { it.rating <= 2 }
        else -> reviews
    }

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
            FloatingActionButton(onClick = onWriteReview) {
                Icon(Icons.Default.Edit, contentDescription = "Написать отзыв")
            }
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

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(error)
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
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = String.format("%.1f", averageRating),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold
                            )
                            RatingBar(rating = averageRating.toFloat())
                            Text(
                                text = "$totalReviews отзывов",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

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
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredReviews.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Отзывов пока нет")
                                Spacer(modifier = Modifier.height(12.dp))
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
                                ReviewCard(
                                    review = review,
                                    onLike = { onToggleLike(review.id) },
                                    onReport = { onReportReview(review.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewCard(
    review: Review,
    onLike: () -> Unit,
    onReport: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = review.user?.username?.firstOrNull()?.uppercase() ?: "U",
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

                RatingBar(rating = review.rating.toFloat())
            }

            if (!review.comment.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = review.comment)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLike) {
                    Icon(Icons.Default.ThumbUp, contentDescription = "Лайк")
                }
                Text(review.likesCount.toString())

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = onReport) {
                    Icon(Icons.Default.Report, contentDescription = "Пожаловаться")
                }
            }
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = input.parse(dateString)
        val output = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        output.format(date ?: Date())
    } catch (_: Exception) {
        dateString
    }
}