package com.travelguide.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.review.CreateReportScreen

@Composable
fun CreateReportRoute(
    container: AppContainer,
    poiId: Int,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit
) {
    val vm: CreateReportViewModel = viewModel(
        key = "create-report-$poiId",
        factory = SimpleViewModelFactory {
            CreateReportViewModel(container.reportRepository)
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) onSubmitted()
    }

    CreateReportScreen(
        poiId = poiId,
        isLoading = state.isLoading,
        error = state.error,
        onBackClick = onBackClick,
        onSubmit = { type, comment ->
            vm.submitPoiReport(poiId, type, comment)
        }
    )
}