package com.travelguide.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.admin.AdminScreen

@Composable
fun AdminRoute(
    container: AppContainer,
    onBackClick: () -> Unit
) {
    val vm: AdminViewModel = viewModel(
        factory = SimpleViewModelFactory {
            AdminViewModel(container.adminRepository)
        }
    )
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    AdminScreen(
        state = state,
        onBackClick = onBackClick,
        onRefresh = vm::load,
        onApproveReview = vm::approveReview,
        onRejectReview = vm::rejectReview,
        onResolveReport = vm::resolveReport,
        onRejectReport = vm::rejectReport,
        onApprovePoiPhoto = vm::approvePoiPhoto,
        onRejectPoiPhoto = vm::rejectPoiPhoto,
        onMessageShown = vm::clearMessage
    )
}
