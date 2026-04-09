package com.travelguide.personalisation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.domain.models.Collection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.travelguide.core.toUserMessage
class CollectionsViewModel(
    private val repository: CollectionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CollectionsUiState())
    val state: StateFlow<CollectionsUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                repository.getCollections(page = 0, size = 100)
            }.onSuccess { collections ->
                _state.value = CollectionsUiState(
                    isLoading = false,
                    collections = collections,
                    errorMessage = null
                )
            }.onFailure { e ->
                _state.value = CollectionsUiState(
                    isLoading = false,
                    collections = emptyList(),
                    errorMessage = e.toUserMessage("Не удалось загрузить коллекции")
                )
            }
        }
    }

    fun createCollection(
        name: String,
        description: String?
    ) {
        viewModelScope.launch {
            runCatching {
                repository.createCollection(
                    name = name,
                    description = description
                )
            }.onSuccess { created ->
                _state.value = _state.value.copy(
                    collections = listOf(created) + _state.value.collections
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    errorMessage = e.toUserMessage("Не удалось создать коллекцию")
                )
            }
        }
    }

    fun deleteCollection(collectionId: Int) {
        viewModelScope.launch {
            runCatching {
                repository.deleteCollection(collectionId)
            }.onSuccess {
                _state.value = _state.value.copy(
                    collections = _state.value.collections.filterNot { it.id == collectionId }
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    errorMessage = e.toUserMessage("Не удалось удалить коллекцию")
                )
            }
        }
    }

    fun updateCollectionLocal(updated: Collection) {
        _state.value = _state.value.copy(
            collections = _state.value.collections.map {
                if (it.id == updated.id) updated else it
            }
        )
    }
}