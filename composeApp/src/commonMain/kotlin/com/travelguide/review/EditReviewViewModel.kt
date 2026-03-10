package com.travelguide.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.domain.models.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditReviewUiState(
    val isLoading: Boolean = false,
    val review: Review? = null,
    val success: Boolean = false,
    val error: String? = null
)

class EditReviewViewModel(
    private val repository: ReviewRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditReviewUiState())
    val state: StateFlow<EditReviewUiState> = _state.asStateFlow()

    fun load(reviewId: Int) {
        _state.value = EditReviewUiState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                val review = repository.getReviewById(reviewId)
                _state.value = EditReviewUiState(review = review)
            }.onFailure {
                _state.value = EditReviewUiState(
                    error = it.message ?: "Не удалось загрузить отзыв"
                )
            }
        }
    }

    fun save(reviewId: Int, rating: Int, comment: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching {
                repository.updateReview(
                    reviewId = reviewId,
                    rating = rating,
                    comment = comment
                )
            }.onSuccess {
                _state.value = _state.value.copy(
                    isLoading = false,
                    success = true
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = it.message ?: "Не удалось сохранить отзыв"
                )
            }
        }
    }
}