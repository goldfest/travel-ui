package com.travelguide.personalisation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.travelguide.core.toUserMessage
class CollectionPickerViewModel(
    private val repository: CollectionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CollectionPickerUiState())
    val state: StateFlow<CollectionPickerUiState> = _state.asStateFlow()

    fun loadCollections() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            runCatching {
                repository.getCollections(page = 0, size = 100)
            }.onSuccess { collections ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    collections = collections
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Не удалось загрузить коллекции")
                )
            }
        }
    }

    fun addPoiToCollection(
        collectionId: Int,
        poiId: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isAdding = true,
                errorMessage = null,
                successMessage = null
            )

            runCatching {
                repository.addPoiToCollection(collectionId, poiId)
            }.onSuccess {
                _state.value = _state.value.copy(
                    isAdding = false,
                    successMessage = "Объект добавлен в коллекцию"
                )
                onSuccess()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isAdding = false,
                    errorMessage = e.toUserMessage("Не удалось добавить объект в коллекцию")
                )
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}