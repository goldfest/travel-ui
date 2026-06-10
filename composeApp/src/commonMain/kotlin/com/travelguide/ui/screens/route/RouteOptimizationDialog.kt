package com.travelguide.ui.screens.route

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.Route
import com.travelguide.route.RouteOptimizationDayForm
import com.travelguide.route.RouteOptimizationForm
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

private sealed class OptimizationTimeTarget {
    data class Day(val dayIndex: Int, val isStart: Boolean) : OptimizationTimeTarget()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteOptimizationDialog(
    route: Route,
    onDismiss: () -> Unit,
    onConfirm: (RouteOptimizationForm) -> Unit
) {
    val sortedDays = remember(route) { route.days.sortedBy { it.dayNumber } }

    var mode by remember(route.optimizationMode) {
        mutableStateOf(
            if (route.optimizationMode?.uppercase() == "USER_ORDER") "USER_ORDER" else "TIME_WINDOW"
        )
    }
    var firstDate by remember(route.id, sortedDays.firstOrNull()?.routeDate) {
        mutableStateOf(sortedDays.firstOrNull()?.routeDate.orEmpty())
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedTimeTarget by remember { mutableStateOf<OptimizationTimeTarget?>(null) }
    var showValidationError by remember { mutableStateOf(false) }

    val daySettings = remember(route.id) {
        mutableStateMapOf<Long, RouteOptimizationDayForm>().apply {
            sortedDays.forEachIndexed { index, day ->
                val existingDate = day.routeDate ?: deriveDate(firstDate, index)
                val start = day.plannedStart?.substringAfter('T')?.take(5) ?: "08:00"
                val end = day.plannedEnd?.substringAfter('T')?.take(5) ?: "18:00"
                put(
                    day.id.toLong(),
                    RouteOptimizationDayForm(
                        routeDayId = day.id.toLong(),
                        routeDate = existingDate,
                        dayStartTime = start,
                        dayEndTime = end
                    )
                )
            }
        }
    }

    fun syncDatesFromFirst(date: String) {
        firstDate = date
        sortedDays.forEachIndexed { index, day ->
            val current = daySettings[day.id.toLong()] ?: return@forEachIndexed
            daySettings[day.id.toLong()] = current.copy(routeDate = deriveDate(date, index))
        }
    }

    val visitDurationInputs = remember(route.id) {
        mutableStateMapOf<Long, String>().apply {
            sortedDays.flatMap { it.points }.forEach { point ->
                put(point.id.toLong(), point.estimatedVisitMinutes.toString())
            }
        }
    }

    val touchedVisitFields = remember(route.id) { mutableStateMapOf<Long, Boolean>() }

    fun isVisitMinutesValid(value: String): Boolean {
        val parsed = value.toIntOrNull()
        return parsed != null && parsed > 0
    }

    fun hasInvalidVisitFields(): Boolean {
        return visitDurationInputs.keys.any { pointId ->
            !isVisitMinutesValid(visitDurationInputs[pointId].orEmpty())
        }
    }

    if (showDatePicker) {
        val initialSelectedDateMillis = firstDate
            .takeIf { it.isNotBlank() }
            ?.let(::dateStringToPickerMillis)

        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialSelectedDateMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            syncDatesFromFirst(pickerMillisToDateString(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("ОК")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    selectedTimeTarget?.let { target ->
        when (target) {
            is OptimizationTimeTarget.Day -> {
                val day = sortedDays[target.dayIndex]
                val current = daySettings[day.id.toLong()] ?: return@let
                val currentValue = if (target.isStart) current.dayStartTime else current.dayEndTime
                val state = rememberDialogTimeState(currentValue)

                AlertDialog(
                    onDismissRequest = { selectedTimeTarget = null },
                    title = { Text(if (target.isStart) "Время начала дня" else "Время окончания дня") },
                    text = { TimeInput(state = state) },
                    confirmButton = {
                        Button(
                            onClick = {
                                val newValue = "%02d:%02d".format(state.hour, state.minute)
                                daySettings[day.id.toLong()] = if (target.isStart) {
                                    current.copy(dayStartTime = newValue)
                                } else {
                                    current.copy(dayEndTime = newValue)
                                }
                                selectedTimeTarget = null
                            }
                        ) {
                            Text("ОК")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { selectedTimeTarget = null }) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Оптимизация маршрута") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column {
                        Text("Режим", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = mode == "TIME_WINDOW", onClick = { mode = "TIME_WINDOW" })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Распределить автоматически")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = mode == "USER_ORDER", onClick = { mode = "USER_ORDER" })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Сохранить порядок пользователя")
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Время посещения будет рассчитано алгоритмом автоматически с учётом начала дня, окончания дня и длительности посещения объектов.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.42f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
                            Text("Дата первого дня", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { showDatePicker = true }) {
                                Text(if (firstDate.isBlank()) "Выбрать дату" else firstDate)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Остальные дни будут выставлены автоматически как следующие календарные дни.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (showValidationError) {
                    item {
                        Text(
                            text = "Заполни корректно длительность посещения для каждой точки. Пустые значения отправлять нельзя.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                items(sortedDays.withIndex().toList(), key = { it.value.id }) { indexedDay ->
                    val index = indexedDay.index
                    val day = indexedDay.value
                    val config = daySettings[day.id.toLong()] ?: return@items

                    DaySettingsCard(
                        title = "День ${day.dayNumber}",
                        date = config.routeDate,
                        startTime = config.dayStartTime,
                        endTime = config.dayEndTime,
                        onStartClick = { selectedTimeTarget = OptimizationTimeTarget.Day(index, true) },
                        onEndClick = { selectedTimeTarget = OptimizationTimeTarget.Day(index, false) },
                        points = day.points.sortedBy { it.orderIndex }.map { point ->
                            val pointId = point.id.toLong()
                            DayPointVisitField(
                                pointId = pointId,
                                pointName = point.poiName ?: point.poi?.name ?: "Точка #${point.orderIndex}",
                                visitDurationMinutes = visitDurationInputs[pointId].orEmpty(),
                                isDurationError = touchedVisitFields[pointId] == true &&
                                        !isVisitMinutesValid(visitDurationInputs[pointId].orEmpty())
                            )
                        },
                        onVisitDurationChange = { pointId, value ->
                            touchedVisitFields[pointId] = true
                            visitDurationInputs[pointId] = value
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    visitDurationInputs.keys.forEach { key ->
                        touchedVisitFields[key] = true
                    }
                    val invalid = hasInvalidVisitFields()
                    showValidationError = invalid
                    if (invalid) return@Button

                    onConfirm(
                        RouteOptimizationForm(
                            optimizationMode = mode,
                            daySettings = sortedDays.mapNotNull { daySettings[it.id.toLong()] },
                            visitMinutesByRoutePointId = visitDurationInputs.mapValues { (_, value) -> value.toInt() }
                        )
                    )
                }
            ) {
                Text("Применить")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private data class DayPointVisitField(
    val pointId: Long,
    val pointName: String,
    val visitDurationMinutes: String,
    val isDurationError: Boolean
)

@Composable
private fun DaySettingsCard(
    title: String,
    date: String,
    startTime: String,
    endTime: String,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    points: List<DayPointVisitField>,
    onVisitDurationChange: (Long, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onStartClick, modifier = Modifier.weight(1f)) {
                    Text("С $startTime")
                }
                OutlinedButton(onClick = onEndClick, modifier = Modifier.weight(1f)) {
                    Text("До $endTime")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            points.forEach { point ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Text(point.pointName, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = point.visitDurationMinutes,
                        onValueChange = { onVisitDurationChange(point.pointId, it.filter(Char::isDigit)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Длительность посещения, мин") },
                        singleLine = true,
                        isError = point.isDurationError,
                        supportingText = {
                            if (point.isDurationError) {
                                Text("Введи число больше 0")
                            } else {
                                Text("Время посещения алгоритм рассчитает сам")
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberDialogTimeState(value: String): TimePickerState {
    val hour = value.substringBefore(':').toIntOrNull() ?: 8
    val minute = value.substringAfter(':', "00").toIntOrNull() ?: 0
    return key(value) {
        rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )
    }
}

private fun deriveDate(firstDate: String, index: Int): String {
    if (firstDate.isBlank()) return ""
    return runCatching {
        LocalDate.parse(firstDate).plus(DatePeriod(days = index)).toString()
    }.getOrDefault(firstDate)
}

private fun pickerMillisToDateString(millis: Long): String {
    return Instant.fromEpochMilliseconds(millis)
        .toLocalDateTime(TimeZone.UTC)
        .date
        .toString()
}

private fun dateStringToPickerMillis(date: String): Long {
    return LocalDate.parse(date)
        .atStartOfDayIn(TimeZone.UTC)
        .toEpochMilliseconds()
}
