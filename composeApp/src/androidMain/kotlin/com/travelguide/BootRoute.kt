package com.travelguide

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
        } catch (_: Exception) {
            onGoMain()
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
                            Button(onClick = { scope.launch { check() } }) { Text("Повторить") }
                            OutlinedButton(onClick = {
                                scope.launch {
                                    container.authRepository.logout()
                                    onGoLogin()
                                }
                            }) { Text("Войти заново") }
                        }
                    }
                }
            }
        }
    }
}
