package com.travelguide.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.travelguide.domain.models.User

data class EditProfileUiState(
    val isSaving: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User,
    state: EditProfileUiState,
    onBackClick: () -> Unit,
    onPickAvatarClick: () -> Unit, // picker запускаем из Route
    onSaveClick: (username: String, phone: String?, avatarUrl: String?, homeCityId: Long?) -> Unit
) {
    // ВАЖНО: ключи remember — чтобы обновлялось когда user поменялся (в т.ч. avatarUrl)
    var username by remember(user.username) { mutableStateOf(user.username) }
    var phone by remember(user.phone) { mutableStateOf(user.phone.orEmpty()) }
    var avatarUrl by remember(user.avatarUrl) { mutableStateOf(user.avatarUrl.orEmpty()) }
    var homeCityIdText by remember(user.homeCityId) { mutableStateOf(user.homeCityId?.toString().orEmpty()) }

    val canSave = username.isNotBlank() && !state.isSaving

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val homeCityId = homeCityIdText.trim().toLongOrNull()
                            onSaveClick(
                                username.trim(),
                                phone.trim().ifBlank { null },
                                avatarUrl.trim().ifBlank { null },
                                homeCityId
                            )
                        },
                        enabled = canSave
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Сохранить")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.error != null) {
                AssistChip(onClick = {}, label = { Text(state.error) })
            }

            val context = LocalContext.current
            val initial = username.firstOrNull()?.uppercase() ?: "?"
            var avatarFailed by remember(avatarUrl) { mutableStateOf(false) }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(96.dp)
                ) {
                    if (avatarUrl.isNotBlank() && !avatarFailed) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(avatarUrl)
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
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Имя пользователя") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Телефон") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Можно оставить, если хочешь вручную менять avatarUrl, но в реальном приложении обычно это скрывают.
            OutlinedTextField(
                value = avatarUrl,
                onValueChange = { avatarUrl = it },
                label = { Text("Avatar URL") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = homeCityIdText,
                onValueChange = { homeCityIdText = it },
                label = { Text("Home City ID") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Можно оставить пустым") }
            )

            OutlinedButton(
                onClick = onPickAvatarClick,
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Загрузить аватар (файл)")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val homeCityId = homeCityIdText.trim().toLongOrNull()
                    onSaveClick(
                        username.trim(),
                        phone.trim().ifBlank { null },
                        avatarUrl.trim().ifBlank { null },
                        homeCityId
                    )
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp))
                } else {
                    Text("Сохранить")
                }
            }
        }
    }
}