package com.travelguide.ui.screens.review

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun CreateReportScreen(
    poiId: Int,
    isLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit,
    onSubmit: (type: String, comment: String) -> Unit,
    onSubmitWithPhotos: (type: String, comment: String, files: List<UploadFile>) -> Unit = { type, comment, _ ->
        onSubmit(type, comment)
    }
) {
    val context = LocalContext.current
    var reportType by remember { mutableStateOf("incorrect_info") }
    var comment by remember { mutableStateOf("") }
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        selectedPhotoUris = uris.take(5)
    }

    val reportTypes = listOf(
        "incorrect_info" to "Неверная информация",
        "closed" to "Место закрыто",
        "offensive_content" to "Оскорбительный контент",
        "spam" to "Спам",
        "other" to "Другое"
    )

    fun submit() {
        val files = selectedPhotoUris.mapNotNull { it.toUploadFile(context) }
        onSubmitWithPhotos(reportType, comment.trim(), files)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сообщить о проблеме") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(
                        onClick = ::submit,
                        enabled = comment.isNotBlank() && !isLoading
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
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Тип проблемы", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    reportTypes.forEach { (type, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reportType = type }
                        ) {
                            RadioButton(selected = reportType == type, onClick = { reportType = type })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            }

            Card(shape = RoundedCornerShape(28.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { if (it.length <= 1000) comment = it },
                        label = { Text("Описание проблемы") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 8
                    )
                    Text(
                        text = "${comment.length}/1000",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Card(shape = RoundedCornerShape(28.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Фото-доказательства", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("До 5 фото. Они будут прикреплены к жалобе для модератора.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(
                            onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Добавить")
                        }
                    }

                    if (selectedPhotoUris.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            selectedPhotoUris.forEach { uri ->
                                Box(
                                    modifier = Modifier
                                        .size(92.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Фото жалобы",
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
                                        Icon(Icons.Default.Close, contentDescription = "Удалить фото", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    AssistChip(
                        onClick = {},
                        label = { Text("Жалоба поступит в админ-панель на проверку") },
                        leadingIcon = { Icon(Icons.Default.DoneAll, contentDescription = null) }
                    )
                }
            }

            if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(
                onClick = ::submit,
                enabled = comment.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Отправить жалобу")
            }
        }
    }
}
