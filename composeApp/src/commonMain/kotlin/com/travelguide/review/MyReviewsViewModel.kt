package com.travelguide.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.domain.models.Review
import com.travelguide.profile.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.travelguide.core.toUserMessage
data class MyReviewsUiState(
    val isLoading: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val error: String? = null
)

class MyReviewsViewModel(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MyReviewsUiState())
    val state: StateFlow<MyReviewsUiState> = _state.asStateFlow()

    fun load() {
        _state.value = MyReviewsUiState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                val me = userRepository.getMe()
                val reviews = reviewRepository.getReviewsByUser(me.id)
                _state.value = MyReviewsUiState(reviews = reviews)
            }.onFailure {
                _state.value = MyReviewsUiState(
                    error = it.toUserMessage("Не удалось загрузить мои отзывы")
                )
            }
        }
    }

    fun deleteReview(reviewId: Int) {
        viewModelScope.launch {
            runCatching {
                reviewRepository.deleteReview(reviewId)
            }.onSuccess {
                _state.value = _state.value.copy(
                    reviews = _state.value.reviews.filterNot { it.id == reviewId }
                )
            }
        }
    }
}