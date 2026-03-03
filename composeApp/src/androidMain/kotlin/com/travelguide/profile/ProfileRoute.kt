package com.travelguide.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.simpleFactory
import com.travelguide.ui.screens.profile.ProfileScreen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

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
        factory = simpleFactory {
            ProfileViewModel(container.userRepository, container.authRepository, container.sessionManager)
        }
    )

    val state by vm.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // загрузка профиля
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.loadMe()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.user != null -> {
                    ProfileScreen(
                        user = state.user!!,
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

                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text(state.error ?: "Не удалось загрузить профиль")
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { vm.loadMe() }) { Text("Повторить") }
                        }
                    }
                }
            }
        }
    }
}