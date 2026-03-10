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
    onMyReviewsClick: () -> Unit,
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
    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (!state.isLoggingOut) vm.loadMe()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                state.isLoggingOut -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.isLoading && user == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                user != null -> {
                    ProfileScreen(
                        user = user,
                        onBackClick = onBackClick,
                        onEditClick = onEditClick,
                        onLogout = { vm.logout(onLogoutNavigate) },
                        onFavoritesClick = onFavoritesClick,
                        onRoutesClick = onRoutesClick,
                        onCollectionsClick = onCollectionsClick,
                        onMyReviewsClick = onMyReviewsClick,
                        onAdminClick = onAdminClick,
                        onChangePasswordClick = onChangePasswordClick,
                        onDeleteAccountClick = onDeleteAccountClick
                    )
                }

                state.hasLoadedOnce -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error ?: "Не удалось загрузить профиль")
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { vm.loadMe(force = true) }) {
                                Text("Повторить")
                            }
                        }
                    }
                }

                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}