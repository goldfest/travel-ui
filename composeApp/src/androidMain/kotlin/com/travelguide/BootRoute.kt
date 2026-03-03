package com.travelguide

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelguide.network.UnauthorizedException
import kotlinx.coroutines.launch

private sealed interface BootState {
    data object Loading : BootState
    data class Error(val message: String) : BootState
}

@Composable
fun BootRoute(
    container: AppContainer,
    onGoLogin: () -> Unit,
    onGoMain: () -> Unit
) {
    var state by remember { mutableStateOf<BootState>(BootState.Loading) }
    val scope = rememberCoroutineScope()

    suspend fun check() {
        state = BootState.Loading

        if (!container.authRepository.isLoggedIn()) {
            onGoLogin()
            return
        }

        try {
            container.userRepository.getMe()
            onGoMain()
        } catch (e: UnauthorizedException) {
            container.authRepository.logout()
            onGoLogin()
        } catch (e: Exception) {
            state = BootState.Error(
                e.message?.takeIf { it.isNotBlank() } ?: "Не удалось подключиться к серверу"
            )
        }
    }

    LaunchedEffect(Unit) { check() }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val s = state) {
            BootState.Loading -> CircularProgressIndicator()

            is BootState.Error -> {
                Card(Modifier.padding(16.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Не удалось запустить приложение", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(s.message, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { scope.launch { check() } }) {
                                Text("Повторить")
                            }
                            OutlinedButton(onClick = {
                                scope.launch {
                                    container.authRepository.logout()
                                    onGoLogin()
                                }
                            }) {
                                Text("Войти заново")
                            }
                        }
                    }
                }
            }
        }
    }
}