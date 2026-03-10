package com.travelguide.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.review.CreateReviewScreen

@Composable
fun CreateReviewRoute(
    container: AppContainer,
    poiId: Int,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit
) {
    val vm: CreateReviewViewModel = viewModel(
        key = "create-review-$poiId",
        factory = SimpleViewModelFactory {
            CreateReviewViewModel(container.reviewRepository)
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) onSubmitted()
    }

    CreateReviewScreen(
        poiId = poiId,
        isLoading = state.isLoading,
        error = state.error,
        onBackClick = onBackClick,
        onSubmit = { rating, comment ->
            vm.submit(poiId, rating, comment)
        }
    )
}