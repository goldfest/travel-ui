package com.travelguide.ui.screens.auth

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.travelguide.auth.AuthUiState

import androidx.compose.ui.res.painterResource
import com.travelguide.R
@Composable
fun LoginScreen(
    state: AuthUiState,
    onLoginClick: (email: String, password: String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val errorText = state.error
    val isBadCredentials =
        errorText?.contains("Invalid email or password", ignoreCase = true) == true ||
            errorText?.contains("Невер", ignoreCase = true) == true
    val isUserNotFound = errorText?.contains("Пользователь не найден", ignoreCase = true) == true
    val isBlocked = errorText?.contains("Пользователь заблокирован", ignoreCase = true) == true
    val isInactive = errorText?.contains("Аккаунт не активен", ignoreCase = true) == true

    val emailError = isBadCredentials || isUserNotFound
    val passwordError = isBadCredentials

    val inlineError = when {
        isUserNotFound -> "Пользователь не найден"
        isBadCredentials -> "Неверный email или пароль"
        isBlocked -> "Пользователь заблокирован"
        isInactive -> "Аккаунт не активен"
        !errorText.isNullOrBlank() -> errorText
        else -> null
    }

    val canSubmit = email.isNotBlank() && password.isNotBlank() && !state.isLoading

    ExplorerAuthScreen(
        title = "ВОЙТИ",
        backgroundPainter = painterResource(R.drawable.auth_bg),
        formContent = {
            LoginFields(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible },
                state = state,
                emailError = emailError,
                passwordError = passwordError,
                inlineError = inlineError,
                onSubmit = { onLoginClick(email.trim(), password) },
                canSubmit = canSubmit
            )
        },
        bottomContent = {
            Text(
                text = "Нет аккаунта?",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.82f),
                textAlign = TextAlign.Center
            )
            ExplorerLinkText(text = "РЕГИСТРАЦИЯ", onClick = onRegisterClick, color = Color.White)
        }
    )
}

@Composable
private fun ColumnScope.LoginFields(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePassword: () -> Unit,
    state: AuthUiState,
    emailError: Boolean,
    passwordError: Boolean,
    inlineError: String?,
    onSubmit: () -> Unit,
    canSubmit: Boolean
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Логин") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        enabled = !state.isLoading,
        isError = emailError,
        shape = MaterialTheme.shapes.large
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Пароль") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onTogglePassword) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        enabled = !state.isLoading,
        isError = passwordError,
        shape = MaterialTheme.shapes.large
    )

    if (!inlineError.isNullOrBlank()) {
        Spacer(Modifier.height(10.dp))
        Text(
            text = inlineError,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }

    Spacer(Modifier.height(18.dp))

    ExplorerPrimaryButton(
        text = "ОТПРАВИТЬ",
        onClick = onSubmit,
        modifier = Modifier.fillMaxWidth(),
        enabled = canSubmit,
        trailing = {
            if (state.isLoading) {
                Spacer(Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.height(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            }
        }
    )
}
