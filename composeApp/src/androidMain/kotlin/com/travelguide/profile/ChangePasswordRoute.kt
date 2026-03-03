package com.travelguide.profile

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.simpleFactory
import com.travelguide.ui.screens.profile.ChangePasswordScreen

@Composable
fun ChangePasswordRoute(
    container: AppContainer,
    onBackClick: () -> Unit,
    onDone: () -> Unit
) {
    val vm: ProfileViewModel = viewModel(
        factory = simpleFactory { ProfileViewModel(container.userRepository, container.authRepository, container.sessionManager) }
    )
    val editState by vm.editState.collectAsState()

    ChangePasswordScreen(
        state = editState,
        onBackClick = onBackClick,
        onSubmit = { current, newPass ->
            vm.changePassword(
                current = current,
                new = newPass,
                onSuccess = onDone
            )
        }
    )
}