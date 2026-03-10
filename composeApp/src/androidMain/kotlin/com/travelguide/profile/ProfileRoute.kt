package com.travelguide.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.profile.ProfileScreen

@Composable
fun ProfileRoute(
    container: AppContainer,
    onBackClick: () -> Unit,
    onLogoutNavigate: () -> Unit,
    onFavoritesClick: () -> Unit,
    onRoutesClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onEditClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    val vm: ProfileViewModel = viewModel(
        factory = SimpleViewModelFactory {
            ProfileViewModel(
                container.userRepository,
                container.authRepository,
                container.sessionManager
            )
        }
    )

    val state by vm.state.collectAsState()

    // если у тебя уже есть events/snackbar — можно подключить позже
    val snackbarHostState = remember { SnackbarHostState() }

    // Загружаем на вход в экран + на возврат (resume),
    // но НЕ показываем "ошибка" во время логаута/редиректа
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // не перезагружаем, если уже выходим
                if (!state.isLoggingOut) vm.loadMe()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // На первый заход (чтобы не ждать ON_RESUME в некоторых кейсах навигации)
    LaunchedEffect(Unit) {
        vm.loadMe()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val user = state.user

            when {
                // 1) во время logout вообще ничего не “ругаем” — просто спиннер
                state.isLoggingOut -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // 2) если нет user и идет загрузка — показываем лоадер (без "не удалось")
                state.isLoading && user == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // 3) профиль есть — рисуем экран
                user != null -> {
                    ProfileScreen(
                        user = user,
                        onBackClick = onBackClick,
                        onEditClick = onEditClick,
                        onLogout = { vm.logout(onLogoutNavigate) },
                        onFavoritesClick = onFavoritesClick,
                        onRoutesClick = onRoutesClick,
                        onCollectionsClick = onCollectionsClick,
                        onAdminClick = onAdminClick,
                        onChangePasswordClick = onChangePasswordClick,
                        onDeleteAccountClick = onDeleteAccountClick
                    )
                }

                // 4) ошибокку показываем только если:
                // - уже пытались загрузить (hasLoadedOnce)
                // - и это не логаут
                state.hasLoadedOnce -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error ?: "Не удалось загрузить профиль")
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { vm.loadMe(force = true) }
                            ) { Text("Повторить") }
                        }
                    }
                }

                // 5) начальное состояние (пока даже не начинали) — просто лоадер
                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}