package com.travelguide.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    if (totalPages <= 1) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPreviousPage,
            enabled = currentPage > 0 && !isLoading
        ) {
            Text("Назад")
        }

        Text(
            text = "Страница ${currentPage + 1} из $totalPages",
            color = labelColor,
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = onNextPage,
            enabled = currentPage < totalPages - 1 && !isLoading
        ) {
            Text("Вперёд")
        }
    }
}
