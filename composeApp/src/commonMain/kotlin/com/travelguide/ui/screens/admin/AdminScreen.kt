package com.travelguide.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.travelguide.admin.AdminUiState
import com.travelguide.core.MediaUrlResolver
import com.travelguide.network.dto.poi.PoiMediaDto
import com.travelguide.network.dto.review.ReportResponseDto
import com.travelguide.network.dto.review.ReviewResponseDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    state: AdminUiState,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onApproveReview: (Long) -> Unit,
    onRejectReview: (Long) -> Unit,
    onResolveReport: (Long) -> Unit,
    onRejectReport: (Long) -> Unit,
    onApprovePoiPhoto: (Long, Long) -> Unit,
    onRejectPoiPhoto: (Long, Long) -> Unit,
    onMessageShown: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Обзор", "Отзывы", "Жалобы", "Фото")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message, state.error) {
        val text = state.message ?: state.error
        if (!text.isNullOrBlank()) {
            snackbarHostState.showSnackbar(text)
            onMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Админ-панель") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !state.isLoading && !state.isActionLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else if (state.isActionLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when {
                state.isLoading && state.pendingReviews.isEmpty() && state.pendingReports.isEmpty() && state.pendingPoiPhotos.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                selectedTab == 0 -> AdminOverview(state = state)
                selectedTab == 1 -> PendingReviewsList(state.pendingReviews, onApproveReview, onRejectReview)
                selectedTab == 2 -> PendingReportsList(state.pendingReports, onResolveReport, onRejectReport)
                selectedTab == 3 -> PendingPoiPhotosList(state.pendingPoiPhotos, onApprovePoiPhoto, onRejectPoiPhoto)
            }
        }
    }
}

@Composable
private fun AdminOverview(state: AdminUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                AdminStatCard("Отзывы", state.dashboard.pendingReviews.toString(), Icons.Default.RateReview, Modifier.weight(1f))
                AdminStatCard("Жалобы", state.dashboard.pendingReports.toString(), Icons.Default.Flag, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                AdminStatCard("Фото POI", state.dashboard.pendingPoiPhotos.toString(), Icons.Default.Image, Modifier.weight(1f))
                AdminStatCard("Всего", (state.dashboard.pendingReviews + state.dashboard.pendingReports + state.dashboard.pendingPoiPhotos).toString(), Icons.Default.Warning, Modifier.weight(1f))
            }
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Очередь модерации", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Здесь отображаются отзывы с фотографиями, жалобы пользователей и новые фото объектов, которые ожидают проверки администратора.")
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PendingReviewsList(
    reviews: List<ReviewResponseDto>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    ModerationListEmptyAware(itemsCount = reviews.size, emptyText = "Отзывов на модерации нет") {
        items(reviews, key = { it.id }) { review ->
            ModerationCard(
                title = "Отзыв #${review.id} • ${review.rating}★",
                subtitle = "POI #${review.poiId} • пользователь #${review.userId}",
                body = review.comment ?: "Комментарий не указан",
                images = review.media.mapNotNull { MediaUrlResolver.resolve(it.imageUrl ?: it.thumbnailUrl) },
                onApprove = { onApprove(review.id) },
                onReject = { onReject(review.id) }
            )
        }
    }
}

@Composable
private fun PendingReportsList(
    reports: List<ReportResponseDto>,
    onResolve: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    ModerationListEmptyAware(itemsCount = reports.size, emptyText = "Жалоб на модерации нет") {
        items(reports, key = { it.id }) { report ->
            val target = when {
                report.reviewId != null -> "отзыв #${report.reviewId}"
                report.poiId != null -> "объект #${report.poiId}"
                else -> "цель не указана"
            }
            ModerationCard(
                title = "Жалоба #${report.id} • ${report.reportType}",
                subtitle = target,
                body = report.comment ?: "Описание не указано",
                images = buildList {
                    MediaUrlResolver.resolve(report.photoUrl)?.let(::add)
                    addAll(report.media.mapNotNull { MediaUrlResolver.resolve(it.imageUrl ?: it.thumbnailUrl) })
                },
                approveText = "Обработать",
                rejectText = "Отклонить",
                onApprove = { onResolve(report.id) },
                onReject = { onReject(report.id) }
            )
        }
    }
}

@Composable
private fun PendingPoiPhotosList(
    photos: List<PoiMediaDto>,
    onApprove: (Long, Long) -> Unit,
    onReject: (Long, Long) -> Unit
) {
    ModerationListEmptyAware(itemsCount = photos.size, emptyText = "Фотографий объектов на модерации нет") {
        items(photos, key = { it.id ?: it.url }) { photo ->
            val poiId = photo.poiId
            val mediaId = photo.id
            ModerationCard(
                title = "Фото объекта #${poiId ?: "?"}",
                subtitle = "Загрузил пользователь #${photo.userId ?: "?"}",
                body = photo.originalFilename ?: "Новое фото ожидает проверки",
                images = listOfNotNull(MediaUrlResolver.resolve(photo.url)),
                onApprove = {
                    if (poiId != null && mediaId != null) onApprove(poiId, mediaId)
                },
                onReject = {
                    if (poiId != null && mediaId != null) onReject(poiId, mediaId)
                }
            )
        }
    }
}

@Composable
private fun ModerationListEmptyAware(
    itemsCount: Int,
    emptyText: String,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    if (itemsCount == 0) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun ModerationCard(
    title: String,
    subtitle: String,
    body: String,
    images: List<String>,
    approveText: String = "Одобрить",
    rejectText: String = "Отклонить",
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(body, maxLines = 5, overflow = TextOverflow.Ellipsis)

            if (images.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    images.take(3).forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .height(104.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) {
                    Text(rejectText)
                }
                Button(onClick = onApprove, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(approveText)
                }
            }
        }
    }
}
