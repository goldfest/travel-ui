package com.travelguide.ui.screens.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(
    poiId: Int,
    isLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit,
    onSubmit: (type: String, comment: String) -> Unit
) {
    var reportType by remember { mutableStateOf("incorrect_info") }
    var comment by remember { mutableStateOf("") }

    val reportTypes = listOf(
        "incorrect_info" to "Неверная информация",
        "closed" to "Место закрыто",
        "offensive_content" to "Оскорбительный контент",
        "spam" to "Спам",
        "other" to "Другое"
    )

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
                        onClick = { onSubmit(reportType, comment) },
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Тип проблемы",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            reportTypes.forEach { (type, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { reportType = type }
                ) {
                    RadioButton(
                        selected = reportType == type,
                        onClick = { reportType = type }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { if (it.length <= 1000) comment = it },
                label = { Text("Описание проблемы") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                maxLines = 8
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${comment.length}/1000",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.End)
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}