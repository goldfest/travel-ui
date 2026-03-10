// screens/auth/RegisterScreen.kt
package com.travelguide.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.travelguide.auth.AuthUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    state: AuthUiState,
    onRegisterClick: (email: String, username: String, password: String, phone: String?) -> Unit,
    onLoginClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    // ---- password rules
    val passMin8 = password.length >= 8
    val passLettersDigits = password.any { it.isLetter() } && password.any { it.isDigit() }
    val passMatch = password.isNotEmpty() && password == confirmPassword
    val passwordOk = passMin8 && passLettersDigits && passMatch

    // ---- server error
    val errorText: String? = state.error

    // Сервер теперь отвечает по-русски
    val isEmailTaken = errorText?.contains("Почта уже используется", ignoreCase = true) == true
    val isUsernameTaken = errorText?.contains("Логин уже используется", ignoreCase = true) == true

    // (опционально) общая ошибка, которую не привязать к полям — можно показать снизу маленьким текстом
    // но раз ты хочешь убрать "надпись сверху", делаем нейтрально:
    val showGenericError =
        !errorText.isNullOrBlank() && !isEmailTaken && !isUsernameTaken

    val canSubmit =
        username.isNotBlank() &&
                email.isNotBlank() &&
                passwordOk &&
                termsAccepted &&
                !state.isLoading

    Scaffold(
        topBar = { TopAppBar(title = { Text("Регистрация") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Создать аккаунт",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ---- Username (логин)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Логин") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
                isError = isUsernameTaken,
                supportingText = {
                    if (isUsernameTaken) Text("Этот логин уже используется")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
                isError = isEmailTaken,
                supportingText = {
                    if (isEmailTaken) Text("Эта почта уже используется")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Password
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
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Confirm password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Подтвердите пароль") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (confirmPasswordVisible) "Скрыть пароль" else "Показать пароль"
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Requirements
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PasswordRequirement("Минимум 8 символов", passMin8)
                PasswordRequirement("Содержит буквы и цифры", passLettersDigits)
                PasswordRequirement("Пароли совпадают", passMatch)
            }

            // ---- Общая ошибка (если не email/логин) — НЕ сверху, а компактно под требованиями
            if (showGenericError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorText ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Terms
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    enabled = !state.isLoading
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Я согласен с ", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { /* TODO */ }, enabled = !state.isLoading) {
                    Text("условиями использования")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---- Submit
            Button(
                onClick = { onRegisterClick(email.trim(), username.trim(), password, null) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = canSubmit
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Зарегистрироваться", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f))
                Text(
                    "или",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Divider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Уже есть аккаунт? ")
                TextButton(onClick = onLoginClick, enabled = !state.isLoading) {
                    Text("Войти")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PasswordRequirement(text: String, isValid: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
        Icon(
            imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    }
}