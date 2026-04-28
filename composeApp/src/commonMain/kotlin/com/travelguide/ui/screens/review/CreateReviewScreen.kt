package com.travelguide.ui.screens.review

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.travelguide.network.upload.UploadFile
import com.travelguide.ui.util.toUploadFile

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateReviewScreen(
    poiId: Int,
    isLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit,
    onSubmit: (rating: Int, comment: String) -> Unit,
    onSubmitWithPhotos: (rating: Int, comment: String, files: List<UploadFile>) -> Unit = { ratingValue, commentValue, _ ->
        onSubmit(ratingValue, commentValue)
    }
) {
    val context = LocalContext.current
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showModerationDialog by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        selectedPhotoUris = uris.take(5)
    }

    LaunchedEffect(isLoading, error) {
        if (!isLoading && error == null && rating > 0 && comment.isNotBlank()) {
            // Диалог показывается после успешного перехода состояния из загрузки в success
            // за навигацию назад отвечает внешний обработчик, если он уже настроен.
        }
    }

    fun submitReview() {
        val files = selectedPhotoUris.mapNotNull { it.toUploadFile(context) }
        onSubmitWithPhotos(rating, comment.trim(), files)
        showModerationDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Оставить отзыв") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(
                        onClick = ::submitReview,
                        enabled = rating > 0 && comment.isNotBlank() && !isLoading
                    ) {
                        Text("Отправить")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(shape = RoundedCornerShape(28.dp)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Оценка",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { rating = star }) {
                                Icon(
                                    imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { if (it.length <= 1000) comment = it },
                        label = { Text("Поделитесь впечатлениями") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 10
                    )

                    Text(
                        text = "${comment.length}/1000",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Card(shape = RoundedCornerShape(28.dp)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Фото к отзыву",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "До 5 фото. После отправки отзыв уйдет на модерацию.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Добавить")
                        }
                    }

                    if (selectedPhotoUris.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            selectedPhotoUris.forEach { uri ->
                                Box(
                                    modifier = Modifier
                                        .size(92.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Фото отзыва",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    IconButton(
                                        onClick = { selectedPhotoUris = selectedPhotoUris - uri },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Удалить фото",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AssistChip(
                        onClick = {},
                        label = { Text("Отзыв появится в приложении только после проверки модератором") },
                        leadingIcon = { Icon(Icons.Default.DoneAll, contentDescription = null) }
                    )
                }
            }

            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(
                onClick = ::submitReview,
                enabled = rating > 0 && comment.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Отправить на модерацию")
            }
        }
    }

    if (showModerationDialog && error == null) {
        AlertDialog(
            onDismissRequest = { showModerationDialog = false },
            icon = { Icon(Icons.Default.DoneAll, contentDescription = null) },
            title = { Text("Отзыв отправлен") },
            text = { Text("Спасибо! Отзыв и прикрепленные фото будут опубликованы после модерации.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showModerationDialog = false
                        onBackClick()
                    }
                ) {
                    Text("Понятно")
                }
            }
        )
    }
}
