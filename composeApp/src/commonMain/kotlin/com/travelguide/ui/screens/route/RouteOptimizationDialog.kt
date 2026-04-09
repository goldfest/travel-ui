package com.travelguide.ui.screens.route

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.Route
import com.travelguide.route.RouteOptimizationForm

@Composable
fun RouteOptimizationDialog(
    route: Route,
    onDismiss: () -> Unit,
    onConfirm: (RouteOptimizationForm) -> Unit
) {
    val routePointIds = route.days.firstOrNull()?.points?.map { it.id.toLong() }.orEmpty()

    var mode by remember { mutableStateOf("TIME_WINDOW") }
    var dayStartTime by remember { mutableStateOf("09:00") }
    var dayEndTime by remember { mutableStateOf("18:00") }
    var maxTotalMinutesPerDayText by remember { mutableStateOf("480") }
    var maxPointsPerDayText by remember { mutableStateOf(routePointIds.size.coerceAtLeast(1).toString()) }
    var maxTravelMinutesText by remember { mutableStateOf("45") }
    var allowDroppingPoints by remember { mutableStateOf(true) }
    var keepFirstAndLast by remember { mutableStateOf(true) }
    var considerLunchBreak by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Параметры оптимизации") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "TIME_WINDOW — уложить маршрут в выбранное окно. USER_ORDER — сохранить текущий порядок точек пользователя и только применить ограничения.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = mode,
                    onValueChange = { mode = it.uppercase() },
                    label = { Text("Режим: TIME_WINDOW / USER_ORDER") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dayStartTime,
                    onValueChange = { dayStartTime = it },
                    label = { Text("Начало дня (HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dayEndTime,
                    onValueChange = { dayEndTime = it },
                    label = { Text("Конец дня (HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxTotalMinutesPerDayText,
                    onValueChange = { maxTotalMinutesPerDayText = it },
                    label = { Text("Максимум минут в день") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxPointsPerDayText,
                    onValueChange = { maxPointsPerDayText = it },
                    label = { Text("Максимум точек в день") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxTravelMinutesText,
                    onValueChange = { maxTravelMinutesText = it },
                    label = { Text("Максимум минут между соседними точками") },
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                CheckRow("Разрешить отбрасывать точки", allowDroppingPoints) { allowDroppingPoints = it }
                CheckRow("Сохранять первую и последнюю точки", keepFirstAndLast) { keepFirstAndLast = it }
                CheckRow("Добавить обед 12:00–13:00", considerLunchBreak) { considerLunchBreak = it }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        RouteOptimizationForm(
                            optimizationMode = mode.ifBlank { "TIME_WINDOW" },
                            dayStartTime = dayStartTime,
                            dayEndTime = dayEndTime,
                            maxTotalMinutesPerDay = maxTotalMinutesPerDayText.toIntOrNull(),
                            maxPointsPerDay = maxPointsPerDayText.toIntOrNull(),
                            maxTravelMinutesBetweenPoints = maxTravelMinutesText.toIntOrNull(),
                            allowDroppingPoints = allowDroppingPoints,
                            keepFirstAndLast = keepFirstAndLast,
                            orderedRoutePointIds = routePointIds,
                            considerLunchBreak = considerLunchBreak
                        )
                    )
                }
            ) { Text("Применить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f).padding(end = 8.dp))
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}
