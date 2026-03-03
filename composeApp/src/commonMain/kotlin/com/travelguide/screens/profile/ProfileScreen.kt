package com.travelguide.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.travelguide.domain.models.User

import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onLogout: () -> Unit,
    onFavoritesClick: () -> Unit,
    onRoutesClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onAdminClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
) {
    val isAdmin = user.isAdmin()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Header карточка
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clickable(onClick = onEditClick),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val context = LocalContext.current
                    val initial = user.username.firstOrNull()?.uppercase() ?: "?"
                    var avatarFailed by remember(user.avatarUrl) { mutableStateOf(false) }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    ) {
                        val url = user.avatarUrl

                        if (!url.isNullOrBlank() && !avatarFailed) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(url)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Аватар",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                onError = { avatarFailed = true }
                            )
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = initial,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.username, style = MaterialTheme.typography.titleLarge)
                            if (isAdmin) {
                                Spacer(Modifier.width(8.dp))
                                AssistChip(
                                    onClick = {},
                                    label = { Text("ADMIN") },
                                    leadingIcon = {
                                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!user.phone.isNullOrBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = user.phone!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Открыть редактирование")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Быстрые действия
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column {
                    Divider()
                    ProfileNavItem("Избранное", Icons.Default.Favorite, onFavoritesClick)
                    Divider()
                    ProfileNavItem("Мои маршруты", Icons.Default.Route, onRoutesClick)
                    Divider()
                    ProfileNavItem("Мои коллекции", Icons.Default.Collections, onCollectionsClick)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Настройки (заглушки)
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column {
                    ListItem(
                        headlineContent = { Text("Темная тема") },
                        leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                        trailingContent = { Switch(checked = false, onCheckedChange = { /* TODO */ }) }
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("Уведомления") },
                        leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable { /* TODO */ }
                    )
                }
            }

            if (isAdmin) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    ProfileNavItem(
                        title = "Админ-панель",
                        icon = Icons.Default.AdminPanelSettings,
                        onClick = onAdminClick,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column {
                    ProfileNavItem(
                        title = "Сменить пароль",
                        icon = Icons.Default.Password,
                        onClick = onChangePasswordClick
                    )
                    Divider()
                    ProfileNavItem(
                        title = "Удалить аккаунт",
                        icon = Icons.Default.DeleteForever,
                        onClick = onDeleteAccountClick,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Выйти", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ProfileNavItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null, tint = tint) },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}