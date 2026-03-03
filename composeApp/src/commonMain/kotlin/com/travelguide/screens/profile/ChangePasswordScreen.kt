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

    val canSubmit = current.isNotBlank() && newPass.length >= 6 && newPass == confirm && !state.isSaving

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
                label = { Text("Новый пароль (мин. 6)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Повторите новый пароль") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onSubmit(current, newPass) },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Сменить пароль")
            }
        }
    }
}