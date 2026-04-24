package com.travelguide

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.travelguide.network.UnauthorizedException
import com.travelguide.ui.screens.auth.ExplorerWelcomeScreen

import androidx.compose.ui.res.painterResource

private sealed interface BootState {
    data object Checking : BootState
    data object Welcome : BootState
}

@Composable
fun BootRoute(
    container: AppContainer,
    onGoLogin: () -> Unit,
    onGoMain: () -> Unit
) {
    var state by remember { mutableStateOf<BootState>(BootState.Checking) }

    suspend fun checkSession() {
        state = BootState.Checking

        if (!container.authRepository.isLoggedIn()) {
            state = BootState.Welcome
            return
        }

        try {
            container.userRepository.getMe()
            onGoMain()
        } catch (e: UnauthorizedException) {
            container.authRepository.logout()
            state = BootState.Welcome
        } catch (_: Exception) {
            onGoMain()
        }
    }

    LaunchedEffect(Unit) { checkSession() }

    when (state) {
        BootState.Checking -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Подготавливаем ваше путешествие",
                    modifier = Modifier.padding(top = 14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        BootState.Welcome -> {
            ExplorerWelcomeScreen(
                title = "НАЗВАНИЕ",
                headline = "ПУТЕШЕСТВИЯ -\nКРУТО",
                primaryActionLabel = "НАЧАТЬ",
                secondaryLabel = "Есть аккаунт? Войти.",
                onPrimaryAction = onGoLogin,
                onSecondaryAction = onGoLogin,
                backgroundPainter = painterResource(R.drawable.auth_bg)
            )
        }
    }
}
