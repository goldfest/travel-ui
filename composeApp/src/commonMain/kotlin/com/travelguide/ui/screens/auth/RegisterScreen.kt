package com.travelguide.ui.screens.auth

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.travelguide.R
import com.travelguide.auth.AuthUiState
import com.travelguide.theme.TravelAccent
import com.travelguide.theme.TravelDark
import com.travelguide.theme.TravelPanel
import com.travelguide.theme.TravelTextPrimary
import com.travelguide.theme.TravelTextSecondary
import com.travelguide.theme.TravelDanger

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
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.94f),
                textAlign = TextAlign.Center
            )
            ExplorerLinkText(
                text = "ВОЙТИ",
                onClick = onLoginClick,
                color = Color.White.copy(alpha = 0.96f)
            )
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
        shape = RoundedCornerShape(24.dp),
        colors = authTextFieldColors()
    )

    Spacer(Modifier.height(14.dp))

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
        shape = RoundedCornerShape(24.dp),
        colors = authTextFieldColors()
    )

    Spacer(Modifier.height(14.dp))

    OutlinedTextField(
        value = phone,
        onValueChange = onPhoneChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Телефон") },
        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        enabled = !state.isLoading,
        shape = RoundedCornerShape(24.dp),
        colors = authTextFieldColors()
    )

    Spacer(Modifier.height(14.dp))

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Почта") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        enabled = !state.isLoading,
        shape = RoundedCornerShape(24.dp),
        colors = authTextFieldColors()
    )

    if (!inlineError.isNullOrBlank()) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = inlineError,
            style = MaterialTheme.typography.bodySmall,
            color = TravelDanger,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }

    Spacer(Modifier.height(28.dp))

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
                    color = TravelDark
                )
            }
        }
    )
}

@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TravelTextPrimary,
    unfocusedTextColor = TravelTextPrimary,
    disabledTextColor = TravelTextPrimary.copy(alpha = 0.55f),
    focusedContainerColor = TravelPanel.copy(alpha = 0.86f),
    unfocusedContainerColor = TravelPanel.copy(alpha = 0.78f),
    disabledContainerColor = TravelPanel.copy(alpha = 0.52f),
    errorContainerColor = TravelPanel.copy(alpha = 0.86f),
    cursorColor = TravelAccent,
    focusedBorderColor = TravelAccent.copy(alpha = 0.82f),
    unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
    disabledBorderColor = Color.Transparent,
    errorBorderColor = TravelDanger,
    focusedPlaceholderColor = TravelTextSecondary,
    unfocusedPlaceholderColor = TravelTextSecondary,
    focusedLeadingIconColor = TravelTextSecondary,
    unfocusedLeadingIconColor = TravelTextSecondary,
    focusedTrailingIconColor = TravelTextSecondary,
    unfocusedTrailingIconColor = TravelTextSecondary
)
