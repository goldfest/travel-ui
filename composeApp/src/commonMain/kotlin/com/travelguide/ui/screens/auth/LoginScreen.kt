// screens/auth/LoginScreen.kt
package com.travelguide.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.travelguide.auth.AuthUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: AuthUiState,
    onLoginClick: (email: String, password: String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Ошибка с сервера (подстрой под свой AuthUiState, если поле называется иначе)
    val errorText: String? = state.error

    // ---- распознаём типы ошибок (под твой бэкенд)
    val isBadCredentials =
        errorText?.contains("Invalid email or password", ignoreCase = true) == true ||
                errorText?.contains("Невер", ignoreCase = true) == true // если русифицируешь

    val isUserNotFound =
        errorText?.contains("Пользователь не найден", ignoreCase = true) == true

    val isBlocked =
        errorText?.contains("Пользователь заблокирован", ignoreCase = true) == true

    val isInactive =
        errorText?.contains("Аккаунт не активен", ignoreCase = true) == true

    // Что подсвечивать:
    // - неверные креды/нет пользователя -> подсветим email (и можно пароль тоже)
    val emailError = isBadCredentials || isUserNotFound
    val passError = isBadCredentials

    // Сообщения под полями
    val emailSupporting: String? = when {
        isUserNotFound -> "Пользователь не найден"
        isBadCredentials -> "Неверный email или пароль"
        else -> null
    }
    val passSupporting: String? = when {
        isBadCredentials -> "Проверьте пароль"
        else -> null
    }

    // Общая ошибка (не привязана к конкретному полю)
    val showGenericError =
        !errorText.isNullOrBlank() &&
                !emailError &&
                !passError &&
                !isBlocked &&
                !isInactive

    val canSubmit = email.isNotBlank() && password.isNotBlank() && !state.isLoading

    Scaffold(
        topBar = { TopAppBar(title = { Text("Вход") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TravelGuide",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Добро пожаловать!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
                isError = emailError,
                supportingText = {
                    if (!emailSupporting.isNullOrBlank()) Text(emailSupporting)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
                isError = passError,
                supportingText = {
                    if (!passSupporting.isNullOrBlank()) Text(passSupporting)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Блокировка/неактивен — компактно под полями (не сверху)
            if (isBlocked || isInactive) {
                val msg = when {
                    isBlocked -> "Пользователь заблокирован"
                    else -> "Аккаунт не активен"
                }
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // “прочая” ошибка — тоже снизу, маленьким текстом
            if (showGenericError) {
                Text(
                    text = errorText ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { /* TODO */ }, enabled = !state.isLoading) {
                    Text("Забыли пароль?")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onLoginClick(email.trim(), password) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = canSubmit
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Войти", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Нет аккаунта? ")
                TextButton(onClick = onRegisterClick, enabled = !state.isLoading) {
                    Text("Зарегистрироваться")
                }
            }
        }
    }
}