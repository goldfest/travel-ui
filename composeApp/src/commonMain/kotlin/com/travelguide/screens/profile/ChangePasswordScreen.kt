package com.travelguide.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.travelguide.validation.PasswordValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    state: EditProfileUiState,
    onBackClick: () -> Unit,
    onSubmit: (current: String, new: String) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    val newPassError = remember(newPass) { PasswordValidator.error(newPass) }
    val confirmError = remember(newPass, confirm) {
        if (confirm.isBlank()) null
        else if (newPass == confirm) null
        else "Пароли не совпадают"
    }

    val canSubmit = current.isNotBlank()
            && PasswordValidator.isStrong(newPass)
            && newPass == confirm
            && !state.isSaving

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Смена пароля") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.error != null) {
                AssistChip(onClick = {}, label = { Text(state.error!!) })
            }

            OutlinedTextField(
                value = current,
                onValueChange = { current = it },
                label = { Text("Текущий пароль") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                label = { Text("Новый пароль") },
                singleLine = true,
                isError = newPassError != null,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(newPassError ?: "Минимум 8 символов, буквы + цифры")
                }
            )

            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Повторите новый пароль") },
                singleLine = true,
                isError = confirmError != null,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (confirmError != null) Text(confirmError!!)
                }
            )

            Button(
                onClick = { onSubmit(current.trim(), newPass.trim()) },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Сменить пароль")
            }
        }
    }
}