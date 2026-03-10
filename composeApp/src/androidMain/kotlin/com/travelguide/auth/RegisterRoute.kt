package com.travelguide.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.ui.screens.auth.RegisterScreen

@Composable
fun RegisterRoute(
    container: AppContainer,
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    val vm: AuthViewModel = viewModel(factory = SimpleViewModelFactory { AuthViewModel(container.authRepository) })
    val state by vm.state.collectAsState()

    RegisterScreen(
        onLoginClick = onLoginClick,
        state = state,
        onRegisterClick = { email, username, password, phone ->
            vm.register(email, username, password, phone, onSuccess = onRegisterSuccess)
        }
    )
}