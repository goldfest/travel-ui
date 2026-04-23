package com.travelguide.ui.screens.auth

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
fun RegisterScreen(
    state: AuthUiState,
    onRegisterClick: (email: String, username: String, password: String, phone: String) -> Unit,
    onLoginClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val inlineError = when {
        state.error.isNullOrBlank() -> null
        state.error!!.contains("already", ignoreCase = true) -> "Такой пользователь уже существует"
        else -> state.error
    }

    val canSubmit = listOf(email, username, password, phone).all { it.isNotBlank() } && !state.isLoading

    ExplorerAuthScreen(
        title = "РЕГИСТРАЦИЯ",
        backgroundPainter = painterResource(R.drawable.auth_bg),
        formContent = {
            RegisterFields(
                email = email,
                onEmailChange = { email = it },
                username = username,
                onUsernameChange = { username = it },
                password = password,
                onPasswordChange = { password = it },
                phone = phone,
                onPhoneChange = { phone = it },
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible },
                inlineError = inlineError,
                state = state,
                canSubmit = canSubmit,
                onSubmit = { onRegisterClick(email.trim(), username.trim(), password, phone.trim()) }
            )
        },
        bottomContent = {
            Text(
                text = "Есть аккаунт?",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.82f),
                textAlign = TextAlign.Center
            )
            ExplorerLinkText(text = "ВОЙТИ", onClick = onLoginClick, color = Color.White)
        }
    )
}

@Composable
private fun ColumnScope.RegisterFields(
    email: String,
    onEmailChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePassword: () -> Unit,
    inlineError: String?,
    state: AuthUiState,
    canSubmit: Boolean,
    onSubmit: () -> Unit
) {
    OutlinedTextField(
        value = username,
        onValueChange = onUsernameChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Логин") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
        enabled = !state.isLoading,
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
        shape = MaterialTheme.shapes.large
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = phone,
        onValueChange = onPhoneChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Телефон") },
        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        enabled = !state.isLoading,
        shape = MaterialTheme.shapes.large
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Почта") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        enabled = !state.isLoading,
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
                CircularProgressIndicator(
                    modifier = Modifier.height(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            }
        }
    )
}
