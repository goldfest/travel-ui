package com.travelguide.ui.screens.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.travelguide.domain.models.Review
import com.travelguide.ui.components.FullScreenPhotoViewer
import com.travelguide.ui.components.RatingBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest

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
        "photo" -> reviews.filter { it.images.isNotEmpty() }
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
                            .padding(16.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
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

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == "all",
                                onClick = { selectedFilter = "all" },
                                label = { Text("Все") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == "positive",
                                onClick = { selectedFilter = "positive" },
                                label = { Text("Положительные") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == "negative",
                                onClick = { selectedFilter = "negative" },
                                label = { Text("Критические") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == "photo",
                                onClick = { selectedFilter = "photo" },
                                label = { Text("С фото") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredReviews.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
    val context = LocalContext.current
    var openedPhotoIndex by remember { mutableStateOf<Int?>(null) }
    val images = review.images.filter { it.isNotBlank() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp)
    ) {
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

            if (!review.comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = review.comment)
            }

            if (images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(images) { index, imageUrl ->
                        Surface(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { openedPhotoIndex = index },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Фото из отзыва",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
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

    openedPhotoIndex?.let { index ->
        FullScreenPhotoViewer(
            images = images,
            initialIndex = index,
            onDismiss = { openedPhotoIndex = null }
        )
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
