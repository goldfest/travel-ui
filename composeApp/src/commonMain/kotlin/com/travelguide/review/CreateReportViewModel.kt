package com.travelguide.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.travelguide.core.toUserMessage
data class CreateReportUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class CreateReportViewModel(
    private val repository: ReportRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateReportUiState())
    val state: StateFlow<CreateReportUiState> = _state.asStateFlow()

    fun submitPoiReport(
        poiId: Int,
        reportType: String,
        comment: String
    ) {
        _state.value = CreateReportUiState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                repository.createPoiReport(
                    poiId = poiId,
                    reportType = reportType,
                    comment = comment
                )
            }.onSuccess {
                _state.value = CreateReportUiState(success = true)
            }.onFailure {
                _state.value = CreateReportUiState(
                    error = it.toUserMessage("Не удалось отправить жалобу")
                )
            }
        }
    }
}