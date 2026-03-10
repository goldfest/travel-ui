package com.travelguide.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.domain.models.Review
import com.travelguide.network.dto.review.PoiReviewStatsResponseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewsUiState(
    val isLoading: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val stats: PoiReviewStatsResponseDto? = null,
    val error: String? = null
)

class ReviewsViewModel(
    private val repository: ReviewRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewsUiState())
    val state: StateFlow<ReviewsUiState> = _state.asStateFlow()

    fun load(poiId: Int) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching {
                val reviews = repository.getReviewsByPoi(poiId)
                val stats = repository.getPoiStats(poiId)
                _state.value = ReviewsUiState(
                    isLoading = false,
                    reviews = reviews,
                    stats = stats
                )
            }.onFailure {
                _state.value = ReviewsUiState(
                    isLoading = false,
                    error = it.message ?: "Не удалось загрузить отзывы"
                )
            }
        }
    }

    fun toggleLike(reviewId: Int) {
        viewModelScope.launch {
            runCatching {
                repository.toggleLike(reviewId)
            }.onSuccess { updated ->
                _state.value = _state.value.copy(
                    reviews = _state.value.reviews.map {
                        if (it.id == updated.id) updated else it
                    }
                )
            }
        }
    }
}