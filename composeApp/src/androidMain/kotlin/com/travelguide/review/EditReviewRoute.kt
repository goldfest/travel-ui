package com.travelguide.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.review.EditReviewScreen

@Composable
fun EditReviewRoute(
    container: AppContainer,
    reviewId: Int,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    val vm: EditReviewViewModel = viewModel(
        key = "edit-review-$reviewId",
        factory = SimpleViewModelFactory {
            EditReviewViewModel(container.reviewRepository)
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(reviewId) {
        vm.load(reviewId)
    }

    LaunchedEffect(state.success) {
        if (state.success) onSaved()
    }

    EditReviewScreen(
        review = state.review,
        isLoading = state.isLoading,
        error = state.error,
        onBackClick = onBackClick,
        onSave = { rating, comment ->
            vm.save(reviewId, rating, comment)
        }
    )
}