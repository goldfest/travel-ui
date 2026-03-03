package com.travelguide.profile

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.simpleFactory
import com.travelguide.ui.screens.profile.DeleteAccountScreen

@Composable
fun DeleteAccountRoute(
    container: AppContainer,
    onBackClick: () -> Unit,
    onDeleted: () -> Unit
) {
    val vm: ProfileViewModel = viewModel(
        factory = simpleFactory { ProfileViewModel(container.userRepository, container.authRepository, container.sessionManager) }
    )
    val editState by vm.editState.collectAsState()

    DeleteAccountScreen(
        state = editState,
        onBackClick = onBackClick,
        onConfirmDelete = {
            vm.deleteAccount(
                onSuccess = onDeleted
            )
        }
    )
}