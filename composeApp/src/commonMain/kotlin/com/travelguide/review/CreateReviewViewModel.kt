package com.travelguide.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.core.toUserMessage
import com.travelguide.network.upload.UploadFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateReviewUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class CreateReviewViewModel(
    private val repository: ReviewRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateReviewUiState())
    val state: StateFlow<CreateReviewUiState> = _state.asStateFlow()

    fun submit(
        poiId: Int,
        rating: Int,
        comment: String,
        files: List<UploadFile> = emptyList()
    ) {
        _state.value = CreateReviewUiState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                repository.createReview(
                    poiId = poiId,
                    rating = rating,
                    comment = comment,
                    files = files
                )
            }.onSuccess {
                _state.value = CreateReviewUiState(success = true)
            }.onFailure {
                _state.value = CreateReviewUiState(
                    isLoading = false,
                    error = it.toUserMessage("Не удалось отправить отзыв")
                )
            }
        }
    }
}
