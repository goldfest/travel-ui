package com.travelguide.ui.screens.personalisation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.travelguide.domain.models.POI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionEditScreen(
    isLoading: Boolean,
    isSaving: Boolean,
    isDeleting: Boolean,
    collectionName: String,
    collectionDescription: String,
    collectionCoverUrl: String,
    pois: List<POI>,
    errorMessage: String?,
    successMessage: String?,
    onBackClick: () -> Unit,
    onSave: (String, String?, String?) -> Unit,
    onRemovePoi: (Int) -> Unit,
    onDeleteCollection: () -> Unit
) {
    var name by remember(collectionName) { mutableStateOf(collectionName) }
    var description by remember(collectionDescription) { mutableStateOf(collectionDescription) }
    var coverUrl by remember(collectionCoverUrl) { mutableStateOf(collectionCoverUrl) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование коллекции") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CollectionCoverPreview(coverUrl, name)

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Название*") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Описание") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5
                            )

                            OutlinedTextField(
                                value = coverUrl,
                                onValueChange = { coverUrl = it },
                                label = { Text("Ссылка на обложку") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    onSave(
                                        name.trim(),
                                        description.trim().ifBlank { null },
                                        coverUrl.trim().ifBlank { null }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = name.isNotBlank() && !isSaving
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(if (isSaving) "Сохраняем…" else "Сохранить изменения")
                            }
                        }
                    }
                }

                errorMessage?.let {
                    item { Text(text = it, color = MaterialTheme.colorScheme.error) }
                }

                successMessage?.let {
                    item { Text(text = it, color = MaterialTheme.colorScheme.primary) }
                }

                item {
                    Text(
                        text = "Объекты в коллекции (${pois.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (pois.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(42.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("В коллекции пока нет объектов")
                            }
                        }
                    }
                } else {
                    items(pois, key = { it.id }) { poi ->
                        CollectionPoiItem(
                            poi = poi,
                            onRemove = { onRemovePoi(poi.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = onDeleteCollection,
                        enabled = !isDeleting
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDeleting) "Удаляем…" else "Удалить коллекцию",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionCoverPreview(coverUrl: String, name: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.9f)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (coverUrl.isNotBlank()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(46.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Обложка коллекции",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun CollectionPoiItem(
    poi: POI,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = poi.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!poi.address.isNullOrBlank()) {
                    Text(
                        text = poi.address.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            TextButton(onClick = onRemove) {
                Text(text = "Удалить", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
