//// screens/notifications/NotificationsScreen.kt
//package com.travelguide.ui.screens.notifications
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.travelguide.data.mock.MockData
//
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
//@Composable
//fun NotificationsScreen(
//    onBackClick: () -> Unit
//) {
//    var showUnreadOnly by remember { mutableStateOf(false) }
//    var selectedType by remember { mutableStateOf<String?>(null) }
//
//    val notifications = listOf(
//        MockData.Notification(
//            id = 1,
//            type = "route_reminder",
//            title = "Напоминание о маршруте",
//            description = "Завтра у вас запланирован маршрут 'Историческая Москва'",
//            isRead = false
//        ),
//        MockData.Notification(
//            id = 2,
//            type = "review",
//            title = "Новый отзыв",
//            description = "Пользователь оставил отзыв на вашу рецензию",
//            isRead = true
//        ),
//        MockData.Notification(
//            id = 3,
//            type = "moderation",
//            title = "Жалоба рассмотрена",
//            description = "Ваша жалоба на объект 'Кремль' была одобрена",
//            isRead = true
//        ),
//        MockData.Notification(
//            id = 4,
//            type = "poi_update",
//            title = "Обновление объекта",
//            description = "Объект 'Красная площадь' был обновлен",
//            isRead = false
//        )
//    )
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Уведомления") },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { /* TODO: настройки */ }) {
//                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
//                    }
//                    IconButton(onClick = { /* TODO: отметить все как прочитанные */ }) {
//                        Icon(Icons.Default.DoneAll, contentDescription = "Отметить все")
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            // Фильтры
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 8.dp)
//            ) {
//                Column(modifier = Modifier.padding(12.dp)) {
//                    Row(
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text("Только непрочитанные")
//                        Switch(
//                            checked = showUnreadOnly,
//                            onCheckedChange = { showUnreadOnly = it }
//                        )
//                    }
//
//                    Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//                    // Типы уведомлений
//                    Text(
//                        text = "Типы уведомлений",
//                        style = MaterialTheme.typography.labelLarge,
//                        modifier = Modifier.padding(bottom = 8.dp)
//                    )
//                    FlowRow(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        listOf(
//                            null to "Все",
//                            "route_reminder" to "Маршруты",
//                            "review" to "Отзывы",
//                            "moderation" to "Модерация",
//                            "poi_update" to "Обновления"
//                        ).forEach { (type, label) ->
//                            FilterChip(
//                                selected = selectedType == type,
//                                onClick = { selectedType = type },
//                                label = { Text(label) }
//                            )
//                        }
//                    }
//                }
//            }
//
//            // Список уведомлений
//            val filteredNotifications = notifications.filter { notification ->
//                (if (showUnreadOnly) !notification.isRead else true) &&
//                        (selectedType?.let { notification.type == it } ?: true)
//            }
//
//            if (filteredNotifications.isEmpty()) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        Icon(
//                            Icons.Default.NotificationsOff,
//                            contentDescription = null,
//                            modifier = Modifier.size(64.dp),
//                            tint = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                        Text(
//                            text = "Уведомлений нет",
//                            style = MaterialTheme.typography.titleMedium
//                        )
//                        Text(
//                            text = "Здесь будут появляться ваши уведомления",
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//            } else {
//                LazyColumn(
//                    modifier = Modifier.fillMaxSize(),
//                    contentPadding = PaddingValues(16.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    items(filteredNotifications) { notification ->
//                        NotificationCard(
//                            notification = notification,
//                            onClick = { /* TODO: обработать клик */ }
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun NotificationCard(
//    notification: MockData.Notification,
//    onClick: () -> Unit
//) {
//    Card(
//        onClick = onClick,
//        colors = CardDefaults.cardColors(
//            containerColor = if (!notification.isRead)
//                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
//            else
//                MaterialTheme.colorScheme.surface
//        ),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.Top
//        ) {
//            // Иконка уведомления
//            Surface(
//                shape = CircleShape,
//                color = when(notification.type) {
//                    "route_reminder" -> MaterialTheme.colorScheme.tertiary
//                    "review" -> MaterialTheme.colorScheme.secondary
//                    "moderation" -> MaterialTheme.colorScheme.primary
//                    "poi_update" -> MaterialTheme.colorScheme.errorContainer
//                    else -> MaterialTheme.colorScheme.surfaceVariant
//                },
//                modifier = Modifier.size(48.dp)
//            ) {
//                Box(contentAlignment = Alignment.Center) {
//                    Text(
//                        text = notification.typeIcon(),
//                        fontSize = 20.sp
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.width(16.dp))
//
//            // Содержимое
//            Column(
//                modifier = Modifier.weight(1f)
//            ) {
//                Row(
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(
//                        text = notification.title,
//                        style = MaterialTheme.typography.bodyLarge,
//                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
//                        maxLines = 1,
//                        modifier = Modifier.weight(1f)
//                    )
//
//                    if (!notification.isRead) {
//                        Surface(
//                            shape = CircleShape,
//                            color = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.size(8.dp)
//                        ) {}
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                Text(
//                    text = notification.description,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    maxLines = 2
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                // Время и действия
//                Row(
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(
//                        text = notification.sentAt,
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        if (!notification.isRead) {
//                            TextButton(
//                                onClick = { /* TODO: отметить как прочитанное */ },
//                                modifier = Modifier.padding(0.dp)
//                            ) {
//                                Text("Прочитано", style = MaterialTheme.typography.labelSmall)
//                            }
//                        }
//                        IconButton(
//                            onClick = { /* TODO: удалить */ },
//                            modifier = Modifier.size(24.dp)
//                        ) {
//                            Icon(
//                                Icons.Default.Delete,
//                                contentDescription = "Удалить",
//                                modifier = Modifier.size(16.dp)
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// Обновим MockData для уведомлений
//object MockData {
//    fun Notification(
//        id: Int,
//        type: String,
//        title: String,
//        description: String,
//        isRead: Boolean = false
//    ): com.travelguide.domain.models.Notification {
//        return com.travelguide.domain.models.Notification(
//            id = id,
//            type = type,
//            title = title,
//            description = description,
//            isRead = isRead,
//            userId = 1
//        )
//    }
//}