package com.travelguide.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.review.ReviewsScreen

@Composable
fun ReviewsRoute(
    container: AppContainer,
    poiId: Int,
    onBackClick: () -> Unit,
    onWriteReview: () -> Unit,
    onReportReview: (Int) -> Unit
) {
    val vm: ReviewsViewModel = viewModel(
        key = "reviews-$poiId",
        factory = SimpleViewModelFactory {
            ReviewsViewModel(container.reviewRepository)
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(poiId) {
        vm.load(poiId)
    }

    ReviewsScreen(
        averageRating = state.stats?.averageRating ?: 0.0,
        totalReviews = state.stats?.totalReviews ?: 0,
        reviews = state.reviews,
        isLoading = state.isLoading,
        error = state.error,
        onBackClick = onBackClick,
        onWriteReview = onWriteReview,
        onToggleLike = { vm.toggleLike(it) },
        onReportReview = onReportReview
    )
}