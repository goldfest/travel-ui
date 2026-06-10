package com.travelguide.personalisation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.travelguide.core.toUserMessage
class CollectionEditViewModel(
    private val repository: CollectionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CollectionEditUiState())
    val state: StateFlow<CollectionEditUiState> = _state.asStateFlow()

    fun load(collectionId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            runCatching {
                val collectionDeferred = async { repository.getCollection(collectionId) }
                val poisDeferred = async { repository.getCollectionPois(collectionId) }

                collectionDeferred.await().copy(
                    pois = poisDeferred.await()
                )
            }.onSuccess { collection ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    collection = collection,
                    pois = collection.pois
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Не удалось загрузить коллекцию")
                )
            }
        }
    }

    fun updateCollection(
        collectionId: Int,
        name: String,
        description: String?,
        coverUrl: String? = null,
        onSuccess: (com.travelguide.domain.models.Collection) -> Unit
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )

            runCatching {
                repository.updateCollection(
                    collectionId = collectionId,
                    name = name,
                    description = description,
                    coverUrl = coverUrl
                )
            }.onSuccess { updated ->
                val merged = updated.copy(pois = _state.value.pois)
                _state.value = _state.value.copy(
                    isSaving = false,
                    collection = merged,
                    successMessage = "Коллекция обновлена"
                )
                onSuccess(merged)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isSaving = false,
                    errorMessage = e.toUserMessage("Не удалось обновить коллекцию")
                )
            }
        }
    }

    fun removePoi(
        collectionId: Int,
        poiId: Int,
        onCollectionChanged: (com.travelguide.domain.models.Collection) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                repository.removePoiFromCollection(collectionId, poiId)
            }.onSuccess {
                val newPois = _state.value.pois.filterNot { it.id == poiId }
                val current = _state.value.collection
                val updatedCollection = current?.copy(
                    poiCount = (current.poiCount - 1).coerceAtLeast(0),
                    pois = newPois
                )

                _state.value = _state.value.copy(
                    pois = newPois,
                    collection = updatedCollection,
                    successMessage = "Объект удалён из коллекции"
                )

                if (updatedCollection != null) {
                    onCollectionChanged(updatedCollection)
                }
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    errorMessage = e.toUserMessage("Не удалось удалить объект из коллекции")
                )
            }
        }
    }

    fun deleteCollection(
        collectionId: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isDeleting = true,
                errorMessage = null
            )

            runCatching {
                repository.deleteCollection(collectionId)
            }.onSuccess {
                _state.value = _state.value.copy(
                    isDeleting = false
                )
                onSuccess()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isDeleting = false,
                    errorMessage = e.toUserMessage("Не удалось удалить коллекцию")
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