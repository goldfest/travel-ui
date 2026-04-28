package com.travelguide.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.core.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repository: AdminRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AdminUiState(isLoading = true))
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    fun load() {
        _state.value = _state.value.copy(isLoading = true, error = null, message = null)
        viewModelScope.launch {
            runCatching { repository.loadModerationQueue() }
                .onSuccess { loaded -> _state.value = loaded.copy(isLoading = false) }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.toUserMessage("Не удалось загрузить админ-панель")
                    )
                }
        }
    }

    fun approveReview(id: Long) = runAction("Отзыв одобрен") { repository.approveReview(id) }
    fun rejectReview(id: Long) = runAction("Отзыв отклонён") { repository.rejectReview(id, "Отклонено модератором") }
    fun resolveReport(id: Long) = runAction("Жалоба обработана") { repository.resolveReport(id, "Проверено модератором") }
    fun rejectReport(id: Long) = runAction("Жалоба отклонена") { repository.rejectReport(id, "Нарушение не подтверждено") }
    fun approvePoiPhoto(poiId: Long, mediaId: Long) = runAction("Фото объекта одобрено") { repository.approvePoiPhoto(poiId, mediaId) }
    fun rejectPoiPhoto(poiId: Long, mediaId: Long) = runAction("Фото объекта отклонено") { repository.rejectPoiPhoto(poiId, mediaId, "Отклонено модератором") }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null, error = null)
    }

    private fun runAction(successMessage: String, block: suspend () -> Unit) {
        _state.value = _state.value.copy(isActionLoading = true, error = null, message = null)
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess {
                    runCatching { repository.loadModerationQueue() }
                        .onSuccess { loaded -> _state.value = loaded.copy(isActionLoading = false, message = successMessage) }
                        .onFailure { _state.value = _state.value.copy(isActionLoading = false, message = successMessage) }
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isActionLoading = false,
                        error = e.toUserMessage("Не удалось выполнить действие")
                    )
                }
        }
    }
}
