@file:OptIn(ExperimentalMaterial3Api::class)

package com.travelguide.ui.screens.poi

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.travelguide.domain.models.Feature
import com.travelguide.domain.models.POI
import com.travelguide.domain.models.PoiWorkingHours
import com.travelguide.theme.TravelAccent
import com.travelguide.theme.TravelDanger
import com.travelguide.theme.TravelDark
import com.travelguide.theme.TravelPanel
import com.travelguide.theme.TravelPanelSoft
import com.travelguide.theme.TravelScrim
import com.travelguide.theme.TravelTextSecondary
import com.travelguide.network.upload.UploadFile
import com.travelguide.ui.components.FullScreenPhotoViewer
import com.travelguide.ui.util.toUploadFile

private val LightInfoBackground = Color(0xFFF4F1E9)
private val LightInfoText = Color(0xFF141414)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun POIDetailScreen(
    poi: POI?,
    isLoading: Boolean,
    isFavorite: Boolean,
    averageRating: Double?,
    reviewCount: Int,
    errorMessage: String?,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onAddToRoute: () -> Unit,
    onAddToCollection: () -> Unit,
    onAddToFavorite: () -> Unit,
    onWriteReview: () -> Unit,
    onViewReviews: () -> Unit,
    onReportProblem: () -> Unit,
    onUploadPoiPhotos: (List<UploadFile>) -> Unit = {},
    isPhotoUploading: Boolean = false,
    photoUploadMessage: String? = null,
    onPhotoUploadMessageShown: () -> Unit = {},
    snackbarHost: @Composable (() -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedUploadUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var openedGalleryIndex by remember { mutableStateOf<Int?>(null) }
    val poiPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { uris ->
        selectedUploadUris = uris.take(8)
        val files = uris.take(8).mapNotNull { it.toUploadFile(context) }
        if (files.isNotEmpty()) {
            onUploadPoiPhotos(files)
        }
    }

    Scaffold(
        containerColor = TravelDark,
        snackbarHost = { snackbarHost?.invoke() },
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (poi != null) {
                        IconButton(onClick = onAddToFavorite) {
                            Icon(
                                imageVector = if (isFavorite) {
                                    Icons.Default.Favorite
                                } else {
                                    Icons.Default.FavoriteBorder
                                },
                                contentDescription = if (isFavorite) {
                                    "Удалить из избранного"
                                } else {
                                    "Добавить в избранное"
                                },
                                tint = if (isFavorite) TravelDanger else Color.White
                            )
                        }

                        IconButton(onClick = onReportProblem) {
                            Icon(
                                imageVector = Icons.Default.Report,
                                contentDescription = "Сообщить о проблеме",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TravelDark,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            if (poi != null && !isLoading && errorMessage == null) {
                BottomAppBar(
                    containerColor = TravelPanel,
                    tonalElevation = 0.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAddToRoute,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TravelAccent,
                                contentColor = TravelDark
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("", maxLines = 1)
                        }

                        Button(
                            onClick = onAddToCollection,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TravelPanelSoft,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("", maxLines = 1)
                        }
                    }
                }
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
                    CircularProgressIndicator(color = TravelAccent)
                }
            }

            errorMessage != null -> {
                ErrorState(
                    modifier = Modifier.padding(paddingValues),
                    message = errorMessage,
                    onRetry = onRetry
                )
            }

            poi == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Объект не найден", color = Color.White)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(TravelDark),
                    contentPadding = PaddingValues(bottom = 104.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item {
                        HeroPhotoSection(poi = poi)
                    }

                    item {
                        ObjectMainInfoSection(
                            poi = poi,
                            averageRating = averageRating,
                            reviewCount = reviewCount,
                            onWriteReview = onWriteReview,
                            onViewReviews = onViewReviews
                        )
                    }

                    item {
                        SectionCard(title = "Описание") {
                            Text(
                                text = poi.description?.takeIf { it.isNotBlank() }
                                    ?: "Описание пока не добавлено.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.92f),
                                lineHeight = 22.sp
                            )
                        }
                    }

                    if (poi.images.isNotEmpty()) {
                        item {
                            PhotoGallerySection(
                                images = poi.images,
                                title = "Галерея фото",
                                onImageClick = { openedGalleryIndex = it }
                            )
                        }
                    }
                    item {
                        LocationSection(poi = poi)
                    }

                    item {
                        SectionCard(title = "График работы") {
                            WorkingHoursSection(hours = poi.hours)
                        }
                    }

                    if (poi.features.isNotEmpty()) {
                        item {
                            FeaturesSection(features = poi.features)
                        }
                    }

                    if (poi.tags.isNotEmpty()) {
                        item {
                            SectionCard(title = "Теги") {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    poi.tags.forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = TravelPanelSoft
                                        ) {
                                            Text(
                                                text = tag,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                modifier = Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 8.dp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        UploadPoiPhotoSection(
                            isUploading = isPhotoUploading,
                            onPickPhotos = {
                                poiPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (photoUploadMessage != null) {
        AlertDialog(
            onDismissRequest = {
                onPhotoUploadMessageShown()
            },
            icon = { Icon(Icons.Default.DoneAll, contentDescription = null, tint = TravelAccent) },
            title = { Text("Фото отправлены") },
            text = {
                Text(
                    text = photoUploadMessage
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                                onPhotoUploadMessageShown()
                    }
                ) {
                    Text("Понятно")
                }
            }
        )
    }

    openedGalleryIndex?.let { index ->
        val images = poi?.images?.filter { it.isNotBlank() }.orEmpty()
        FullScreenPhotoViewer(
            images = images,
            initialIndex = index,
            onDismiss = { openedGalleryIndex = null }
        )
    }
}

@Composable
private fun HeroPhotoSection(poi: POI) {
    val context = LocalContext.current
    val images = poi.images.filter { it.isNotBlank() }
    val listState = rememberLazyListState()
    val currentPhoto by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex.coerceAtMost((images.size - 1).coerceAtLeast(0))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(285.dp)
    ) {
        if (images.isNotEmpty()) {
            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(images) { imageUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = poi.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .height(285.dp)
                    )
                }
            }
        } else {
            PoiTypeFallbackPhoto(
                poi = poi,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Transparent,
                            TravelScrim
                        )
                    )
                )
        )

        if (images.size > 1) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.Black.copy(alpha = 0.48f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = "${currentPhoto + 1} / ${images.size}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PoiTypeFallbackPhoto(
    poi: POI,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val icon = poi.poiType?.icon?.trim()

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF5B6058),
                        Color(0xFF2B312C),
                        TravelPanel
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!icon.isNullOrBlank() && icon.isImageUrl()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(icon)
                    .crossfade(true)
                    .build(),
                contentDescription = poi.poiType?.name ?: poi.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(118.dp)
                    .clip(RoundedCornerShape(30.dp))
            )
        } else {
            Text(
                text = icon?.takeIf { it.isNotBlank() } ?: "📍",
                fontSize = 56.sp,
                color = Color.White
            )
        }
    }
}

private fun String.isImageUrl(): Boolean {
    return startsWith("http://", ignoreCase = true) ||
            startsWith("https://", ignoreCase = true) ||
            startsWith("content://", ignoreCase = true)
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ObjectMainInfoSection(
    poi: POI,
    averageRating: Double?,
    reviewCount: Int,
    onWriteReview: () -> Unit,
    onViewReviews: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(TravelPanel)
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = poi.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DarkInfoChip(
                    icon = Icons.Default.Visibility,
                    text = poi.poiType?.name ?: "Объект"
                )

                DarkInfoChip(
                    icon = Icons.Default.Star,
                    text = if (averageRating != null && reviewCount > 0) {
                        "${String.format("%.1f", averageRating)} • $reviewCount отзывов"
                    } else {
                        "Нет отзывов"
                    }
                )

                DarkInfoChip(
                    text = poi.priceLevelText()
                )

                if (poi.latitude != null && poi.longitude != null) {
                    DarkInfoChip(
                        icon = Icons.Default.LocationOn,
                        text = "Есть на карте"
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DarkInfoPointRow(
                    icon = Icons.Default.Visibility,
                    title = "Тип объекта",
                    value = poi.poiType?.name ?: "Не указан"
                )

                DarkInfoPointRow(
                    icon = Icons.Default.LocationOn,
                    title = "Адрес",
                    value = poi.address?.takeIf { it.isNotBlank() } ?: "Адрес не указан"
                )

                DarkInfoPointRow(
                    icon = Icons.Default.Star,
                    title = "Отзывы",
                    value = if (averageRating != null && reviewCount > 0) {
                        "Рейтинг ${String.format("%.1f", averageRating)} на основе $reviewCount отзывов"
                    } else {
                        "Пока нет отзывов"
                    },
                    onClick = onViewReviews
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onWriteReview,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TravelAccent,
                        contentColor = TravelDark
                    )
                ) {
                    Text(
                        text = "Оставить отзыв",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onViewReviews,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TravelPanelSoft,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Все отзывы",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DarkInfoChip(
    text: String,
    icon: ImageVector? = null
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = TravelPanelSoft
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TravelAccent,
                    modifier = Modifier.size(15.dp)
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun DarkInfoPointRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(Color.White.copy(alpha = 0.035f))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.035f))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = TravelAccent.copy(alpha = 0.14f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TravelAccent,
                modifier = Modifier
                    .padding(9.dp)
                    .size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = TravelTextSecondary
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun PhotoGallerySection(
    images: List<String>,
    title: String,
    onImageClick: (Int) -> Unit
) {
    val visibleImages = images.filter { it.isNotBlank() }
    if (visibleImages.isEmpty()) return

    SectionCard(title = title) {
        GalleryMosaic(images = visibleImages, onImageClick = onImageClick)
    }
}

@Composable
private fun GalleryMosaic(
    images: List<String>,
    onImageClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val first = images.getOrNull(0)
    val second = images.getOrNull(1)
    val third = images.getOrNull(2)
    val hiddenCount = (images.size - 3).coerceAtLeast(0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (first != null) {
            GalleryImage(
                imageUrl = first,
                contentDescription = "Фото 1",
                modifier = Modifier
                    .weight(1f)
                    .height(172.dp)
                    .clickable { onImageClick(0) },
                context = context
            )
        }

        Column(
            modifier = Modifier.weight(1.42f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (second != null) {
                GalleryImage(
                    imageUrl = second,
                    contentDescription = "Фото 2",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(82.dp)
                        .clickable { onImageClick(1) },
                    context = context
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (third != null) {
                    GalleryImage(
                        imageUrl = third,
                        contentDescription = "Фото 3",
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clickable { onImageClick(2) },
                        context = context
                    )
                }

                if (hiddenCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF96969C))
                            .clickable { onImageClick(3) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$hiddenCount+",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier,
    context: android.content.Context
) {
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(14.dp))
    )
}

@Composable
private fun UploadPoiPhotoSection(
    isUploading: Boolean,
    onPickPhotos: () -> Unit
) {
    SectionCard(title = "Добавить фото") {
        Text(
            text = "Загрузите свои фотографии объекта. Они попадут на модерацию и появятся в галерее после проверки.",
            style = MaterialTheme.typography.bodyMedium,
            color = TravelTextSecondary,
            lineHeight = 20.sp
        )

        Button(
            onClick = onPickPhotos,
            enabled = !isUploading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TravelAccent,
                contentColor = TravelDark
            )
        ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isUploading) "Загрузка..." else "Загрузить фото")
        }

        AssistChip(
            onClick = {},
            label = { Text("Публикация только после модерации") },
            leadingIcon = { Icon(Icons.Default.DoneAll, contentDescription = null) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeaturesSection(features: List<Feature>) {
    val visibleFeatures = features
        .filter { it.value.equals("true", ignoreCase = true) || it.value.isNotBlank() }
        .groupBy { it.key.substringBefore('.', missingDelimiterValue = "Особенности") }

    SectionCard(title = "Особенности") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            visibleFeatures.forEach { (group, groupFeatures) ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = group,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupFeatures.forEach { feature ->
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = TravelAccent.copy(alpha = 0.14f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = TravelAccent
                                    )
                                    Text(
                                        text = feature.displayName(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.92f)
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

private fun Feature.displayName(): String {
    val title = key.substringAfter('.', key)
    return if (value.equals("true", ignoreCase = true)) title else "$title: $value"
}

@Composable
private fun LocationSection(poi: POI) {
    SectionCard(title = "Расположение") {
        poi.address?.takeIf { it.isNotBlank() }?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TravelAccent,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (poi.latitude != null && poi.longitude != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                PoiLocationMap(
                    latitude = poi.latitude,
                    longitude = poi.longitude,
                    title = poi.name,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = TravelPanelSoft
            ) {
                Text(
                    text = "Координаты для отображения на карте не указаны.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TravelTextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(30.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TravelPanel)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )

            content()
        }
    }
}

@Composable
private fun WorkingHoursSection(hours: List<PoiWorkingHours>) {
    if (hours.isEmpty()) {
        Text(
            text = "График работы не указан",
            style = MaterialTheme.typography.bodyMedium,
            color = TravelTextSecondary
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        hours
            .sortedWith(compareBy({ normalizeDayOfWeek(it.dayOfWeek) }, { !it.isToday }))
            .forEach { entry ->
                val isToday = entry.isToday

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isToday) {
                        TravelPanelSoft
                    } else {
                        Color.White.copy(alpha = 0.03f)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = TravelAccent
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = dayOfWeekLabel(entry.dayOfWeek),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }

                        Text(
                            text = formatHours(entry),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isToday) {
                                TravelAccent
                            } else {
                                TravelTextSecondary
                            }
                        )
                    }
                }
            }
    }
}

@Composable
private fun ErrorState(
    modifier: Modifier,
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TravelPanel)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Ошибка загрузки объекта",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TravelDanger
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(onClick = onRetry) {
                    Text("Повторить")
                }
            }
        }
    }
}

private fun formatHours(hours: PoiWorkingHours): String {
    if (hours.aroundTheClock) return "Круглосуточно"

    val open = formatClockValue(hours.openTime)
    val close = formatClockValue(hours.closeTime)

    return if (!open.isNullOrBlank() && !close.isNullOrBlank()) {
        "$open — $close"
    } else {
        "Закрыто"
    }
}

private fun formatClockValue(value: String?): String? {
    if (value.isNullOrBlank()) return null
    return value.take(5)
}

private fun dayOfWeekLabel(dayOfWeek: Int?): String = when (dayOfWeek) {
    1 -> "Понедельник"
    2 -> "Вторник"
    3 -> "Среда"
    4 -> "Четверг"
    5 -> "Пятница"
    6 -> "Суббота"
    7 -> "Воскресенье"
    else -> "Не указано"
}

private fun normalizeDayOfWeek(dayOfWeek: Int?): Int = when (dayOfWeek) {
    in 1..7 -> dayOfWeek ?: 8
    else -> 8
}