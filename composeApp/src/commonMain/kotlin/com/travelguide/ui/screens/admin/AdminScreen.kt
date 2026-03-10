// screens/admin/AdminScreen.kt
package com.travelguide.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import com.travelguide.domain.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Обзор", "Пользователи", "Модерация", "Статистика")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Админ-панель") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: обновить */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> AdminOverview()
                1 -> AdminUsers()
                2 -> AdminModeration()
                3 -> AdminStatistics()
            }
        }
    }
}

@Composable
fun AdminOverview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Статистика
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard(
                title = "Пользователи",
                value = "1,234",
                icon = Icons.Default.People,
                color = MaterialTheme.colorScheme.primary
            )
            StatCard(
                title = "Объекты",
                value = "5,678",
                icon = Icons.Default.Place,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard(
                title = "Отзывы",
                value = "12,345",
                icon = Icons.Default.Comment,
                color = MaterialTheme.colorScheme.tertiary
            )
            StatCard(
                title = "Маршруты",
                value = "890",
                icon = Icons.Default.Route,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Быстрые действия
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Быстрые действия",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminActionItem(
                        title = "Добавить город",
                        description = "Добавьте новый город в систему",
                        icon = Icons.Default.AddLocation,
                        onClick = { /* TODO */ }
                    )
                    AdminActionItem(
                        title = "Модерация отзывов",
                        description = "Проверьте новые отзывы",
                        icon = Icons.Default.Comment,
                        onClick = { /* TODO */ }
                    )
                    AdminActionItem(
                        title = "Импорт данных",
                        description = "Импорт POI из внешних источников",
                        icon = Icons.Default.Download,
                        onClick = { /* TODO */ }
                    )
                    AdminActionItem(
                        title = "Управление пользователями",
                        description = "Блокировка/разблокировка",
                        icon = Icons.Default.AdminPanelSettings,
                        onClick = { /* TODO */ }
                    )
                }
            }
        }

        // Последние активности
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Последние активности",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(listOf(
                        "Новый пользователь зарегистрировался",
                        "Добавлен объект 'Музей космонавтики'",
                        "Оставлен отзыв на 'Кремль'",
                        "Создан маршрут 'Вечерняя Москва'",
                        "Жалоба на отзыв рассмотрена"
                    )) { activity ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Circle,
                                        contentDescription = null,
                                        modifier = Modifier.size(8.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = activity,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(

    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AdminActionItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AdminUsers() {
    val users = listOf(
        User(
            id = 1,
            email = "admin@example.com",
            username = "Администратор",
            role = "ADMIN"
        ),
        User(
            id = 2,
            email = "moderator@example.com",
            username = "Модератор",
            role = "MODERATOR"
        ),
        User(
            id = 3,
            email = "user@example.com",
            username = "Обычный пользователь",
            role = "USER"
        ),
        User(
            id = 4,
            email = "blocked@example.com",
            username = "Заблокированный",
            role = "USER",
            isBlocked = true
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Пользователи (${users.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { /* TODO: добавить пользователя */ },
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Добавить")
                }
            }
        }

        items(users) { user ->
            UserCard(user = user)
        }
    }
}

@Composable
fun UserCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Аватар
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = user.username.first().toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Роль и статус
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when(user.role) {
                            "ADMIN" -> MaterialTheme.colorScheme.errorContainer
                            "MODERATOR" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    ) {
                        Text(
                            text = user.role,
                            style = MaterialTheme.typography.labelSmall,
                            color = when(user.role) {
                                "ADMIN" -> MaterialTheme.colorScheme.onErrorContainer
                                "MODERATOR" -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (user.isBlocked) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Заблокирован",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Действия
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (user.isBlocked) {
                    Button(
                        onClick = { /* TODO: разблокировать */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Разблокировать")
                    }
                } else {
                    Button(
                        onClick = { /* TODO: заблокировать */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Заблокировать")
                    }
                }

                if (user.role != "ADMIN") {
                    Button(
                        onClick = { /* TODO: изменить роль */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Роль")
                    }
                }

                IconButton(onClick = { /* TODO: подробнее */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Действия")
                }
            }
        }
    }
}

@Composable
fun AdminModeration() {
    var selectedCategory by remember { mutableStateOf("reports") } // reports, reviews, poi

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Категории модерации
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                "reports" to "Жалобы",
                "reviews" to "Отзывы",
                "poi" to "Объекты"
            ).forEach { (category, title) ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(title) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when (selectedCategory) {
            "reports" -> ModerationReports()
            "reviews" -> ModerationReviews()
            "poi" -> ModerationPOI()
        }
    }
}

@Composable
fun ModerationReports() {
    val reports = listOf(
        com.travelguide.domain.models.Report(
            id = 1,
            reportType = "incorrect_info",
            comment = "Неверный адрес, объект находится на другой улице",
            status = "pending",
            userId = 1,
            poiId = 1
        ),
        com.travelguide.domain.models.Report(
            id = 2,
            reportType = "offensive_content",
            comment = "Оскорбительный отзыв",
            status = "pending",
            userId = 2,
            reviewId = 1
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Ожидают рассмотрения (${reports.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(reports) { report ->
            ReportModerationCard(report = report)
        }
    }
}

@Composable
fun ReportModerationCard(report: com.travelguide.domain.models.Report) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when(report.status) {
                "pending" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                "approved" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                "rejected" -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = report.reportTypeText(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when(report.status) {
                        "pending" -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        "approved" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = report.statusText(),
                        style = MaterialTheme.typography.labelSmall,
                        color = when(report.status) {
                            "pending" -> MaterialTheme.colorScheme.error
                            "approved" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = report.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Действия
            if (report.status == "pending") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* TODO: одобрить */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Одобрить")
                    }
                    Button(
                        onClick = { /* TODO: отклонить */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отклонить")
                    }
                }
            } else {
                Text(
                    text = "Обработано",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ModerationReviews() {
    // TODO: Реализация модерации отзывов
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Модерация отзывов")
    }
}

@Composable
fun ModerationPOI() {
    // TODO: Реализация модерации объектов
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Модерация объектов")
    }
}

@Composable
fun AdminStatistics() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Статистика",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Активность пользователей",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                // TODO: Графики статистики
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("График активности")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Популярные объекты",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Кремль" to "1,234 просмотров",
                        "Красная площадь" to "987 просмотров",
                        "Третьяковская галерея" to "765 просмотров",
                        "ВДНХ" to "654 просмотров",
                        "Арбат" to "543 просмотров"
                    ).forEach { (name, views) ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                            Text(views, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}