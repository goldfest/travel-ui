package com.travelguide.ui.screens.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.Review

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReviewScreen(
    review: Review?,
    isLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit,
    onSave: (rating: Int, comment: String) -> Unit
) {
    var rating by remember(review?.id) { mutableStateOf(review?.rating ?: 0) }
    var comment by remember(review?.id) { mutableStateOf(review?.comment ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать отзыв") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onSave(rating, comment) },
                        enabled = rating > 0 && comment.isNotBlank() && !isLoading
                    ) {
                        Text("Сохранить")
                    }
                }
            )
        }
    ) { padding ->
        if (review == null && isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
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

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { if (it.length <= 1000) comment = it },
                    label = { Text("Текст отзыва") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    maxLines = 10
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(error, color = MaterialTheme.colorScheme.error)
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}