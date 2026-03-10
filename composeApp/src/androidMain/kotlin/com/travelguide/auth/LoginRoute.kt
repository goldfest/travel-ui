package com.travelguide.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.ui.screens.auth.LoginScreen

@Composable
fun LoginRoute(
    container: AppContainer,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val vm: AuthViewModel = viewModel(factory = SimpleViewModelFactory { AuthViewModel(container.authRepository) })
    val state by vm.state.collectAsState()

    LoginScreen(
        onRegisterClick = onRegisterClick,
        state = state,
        onLoginClick = { email, password ->
            vm.login(email, password, onSuccess = onLoginSuccess)
        }
    )
}