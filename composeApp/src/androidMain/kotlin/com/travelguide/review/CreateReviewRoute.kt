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


    CreateReviewScreen(
        poiId = poiId,
        isLoading = state.isLoading,
        success = state.success,
        error = state.error,
        onBackClick = onBackClick,
        onSubmitted = onSubmitted,
        onSubmit = { rating, comment ->
            vm.submit(poiId, rating, comment)
        },
        onSubmitWithPhotos = { rating, comment, files ->
            vm.submit(
                poiId = poiId,
                rating = rating,
                comment = comment,
                files = files
            )
        }
    )
}
