package com.travelguide.ui.screens.poi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToRouteActionSheet(
    onDismiss: () -> Unit,
    onCreateNew: () -> Unit,
    onChooseExisting: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Добавить объект в маршрут")

            Button(
                onClick = onCreateNew,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Создать новый маршрут")
            }

            Button(
                onClick = onChooseExisting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Добавить в существующий")
            }
        }
    }
}