package com.travelguide.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.SimpleViewModelFactory
import com.travelguide.ui.screens.review.MyReviewsScreen

@Composable
fun MyReviewsRoute(
    container: AppContainer,
    onBackClick: () -> Unit,
    onEditReview: (Int) -> Unit
) {
    val vm: MyReviewsViewModel = viewModel(
        factory = SimpleViewModelFactory {
            MyReviewsViewModel(
                reviewRepository = container.reviewRepository,
                userRepository = container.userRepository
            )
        }
    )

    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    MyReviewsScreen(
        reviews = state.reviews,
        isLoading = state.isLoading,
        error = state.error,
        onBackClick = onBackClick,
        onEditReview = onEditReview,
        onDeleteReview = { vm.deleteReview(it) }
    )
}