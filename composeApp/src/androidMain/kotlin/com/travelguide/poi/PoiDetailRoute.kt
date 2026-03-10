package com.travelguide.poi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.poi.POIDetailScreen

@Composable
fun PoiDetailRoute(
    container: AppContainer,
    poiId: Int,
    onBackClick: () -> Unit,
    onAddToRoute: () -> Unit,
    onAddToFavorite: (Boolean) -> Unit,
    onWriteReview: () -> Unit,
    onViewReviews: () -> Unit,
    onReportProblem: () -> Unit
) {
    val vm: PoiViewModel = viewModel(
        key = "poi-$poiId",
        factory = SimpleViewModelFactory { PoiViewModel(container.poiRepository) }
    )

    val state by vm.detailsState.collectAsState()

    LaunchedEffect(poiId) {
        vm.loadPoi(poiId)
    }

    POIDetailScreen(
        poi = state.poi,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRetry = { vm.loadPoi(poiId) },
        onBackClick = onBackClick,
        onAddToRoute = onAddToRoute,
        onAddToFavorite = onAddToFavorite,
        onWriteReview = onWriteReview,
        onViewReviews = onViewReviews,
        onReportProblem = onReportProblem
    )
}