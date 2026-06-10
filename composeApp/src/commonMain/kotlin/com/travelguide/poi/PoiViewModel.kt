package com.travelguide.poi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.domain.models.PoiCardUiModel
import com.travelguide.network.upload.UploadFile
import com.travelguide.favorite.FavoriteRepository
import com.travelguide.review.ReviewRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.travelguide.core.toUserMessage

private const val POI_PAGE_SIZE = 10

class PoiViewModel(
    private val repository: PoiRepository,
    private val favoriteRepository: FavoriteRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(PoiListUiState(pageSize = POI_PAGE_SIZE))
    val listState: StateFlow<PoiListUiState> = _listState.asStateFlow()

    private val _detailsState = MutableStateFlow(PoiDetailsUiState())
    val detailsState: StateFlow<PoiDetailsUiState> = _detailsState.asStateFlow()

    fun loadPoisByCity(cityId: Int, page: Int = 0) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                val poisDeferred = async {
                    repository.getPoisByCityPage(
                        cityId = cityId,
                        page = page,
                        size = POI_PAGE_SIZE
                    )
                }
                val typesDeferred = async {
                    if (_listState.value.poiTypes.isEmpty()) repository.getPoiTypes()
                    else _listState.value.poiTypes
                }

                val poisPage = poisDeferred.await()
                val types = typesDeferred.await()
                val items = buildPoiCardItems(poisPage.content)

                _listState.value = PoiListUiState(
                    isLoading = false,
                    items = items,
                    poiTypes = types,
                    currentPage = poisPage.page,
                    pageSize = poisPage.size.takeIf { it > 0 } ?: POI_PAGE_SIZE,
                    totalPages = poisPage.totalPages,
                    totalElements = poisPage.totalElements,
                    errorMessage = null
                )
            }.onFailure { e ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Не удалось загрузить достопримечательности")
                )
            }
        }
    }

    fun searchPoisInCity(
        cityId: Int,
        query: String,
        poiTypeIds: List<Int> = emptyList(),
        page: Int = 0
    ) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            runCatching {
                val poisPage = if (query.isBlank() && poiTypeIds.isEmpty()) {
                    repository.getPoisByCityPage(
                        cityId = cityId,
                        page = page,
                        size = POI_PAGE_SIZE
                    )
                } else {
                    repository.searchPoisPage(
                        cityId = cityId,
                        query = query,
                        poiTypeIds = poiTypeIds,
                        page = page,
                        size = POI_PAGE_SIZE
                    )
                }

                val items = buildPoiCardItems(poisPage.content)

                _listState.value = _listState.value.copy(
                    isLoading = false,
                    items = items,
                    currentPage = poisPage.page,
                    pageSize = poisPage.size.takeIf { it > 0 } ?: POI_PAGE_SIZE,
                    totalPages = poisPage.totalPages,
                    totalElements = poisPage.totalElements,
                    errorMessage = null
                )
            }.onFailure { e ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Ошибка поиска достопримечательностей")
                )
            }
        }
    }

    fun loadPoi(id: Int) {
        viewModelScope.launch {
            _detailsState.value = PoiDetailsUiState(isLoading = true)

            runCatching {
                val poi = repository.getPoiById(id)

                val isFavorite = runCatching {
                    favoriteRepository.isFavorite(id)
                }.getOrDefault(false)

                val stats = runCatching {
                    reviewRepository.getPoiStats(id)
                }.getOrNull()

                _detailsState.value = PoiDetailsUiState(
                    isLoading = false,
                    poi = poi,
                    isFavorite = isFavorite,
                    averageRating = stats?.averageRating,
                    reviewCount = stats?.totalReviews?.toInt() ?: 0,
                    errorMessage = null
                )
            }.onFailure { e ->
                _detailsState.value = PoiDetailsUiState(
                    isLoading = false,
                    poi = null,
                    isFavorite = false,
                    averageRating = null,
                    reviewCount = 0,
                    errorMessage = e.toUserMessage("Не удалось загрузить объект")
                )
            }
        }
    }

    fun uploadPoiPhotos(files: List<UploadFile>) {
        val poi = _detailsState.value.poi ?: return
        if (files.isEmpty()) return

        viewModelScope.launch {
            _detailsState.value = _detailsState.value.copy(
                isPhotoUploading = true,
                photoUploadMessage = null,
                errorMessage = null
            )

            runCatching {
                repository.uploadPoiPhotos(poi.id, files)
            }.onSuccess {
                _detailsState.value = _detailsState.value.copy(
                    isPhotoUploading = false,
                    photoUploadMessage = "Фото отправлены на модерацию. После одобрения они появятся в галерее объекта."
                )
            }.onFailure { e ->
                _detailsState.value = _detailsState.value.copy(
                    isPhotoUploading = false,
                    errorMessage = e.toUserMessage("Не удалось загрузить фото")
                )
            }
        }
    }

    fun clearPhotoUploadMessage() {
        _detailsState.value = _detailsState.value.copy(photoUploadMessage = null)
    }

    fun toggleFavorite() {
        val poi = _detailsState.value.poi ?: return

        viewModelScope.launch {
            val current = _detailsState.value.isFavorite
            val newValue = !current

            runCatching {
                if (current) {
                    favoriteRepository.removeFromFavorites(poi.id)
                } else {
                    favoriteRepository.addToFavorites(poi.id)
                }
            }.onSuccess {
                updateFavoriteInDetails(poi.id, newValue)
                updateFavoriteInList(poi.id, newValue)
            }.onFailure { e ->
                _detailsState.value = _detailsState.value.copy(
                    errorMessage = e.toUserMessage("Не удалось обновить избранное")
                )
            }
        }
    }

    fun toggleFavoriteForCard(poiId: Int) {
        viewModelScope.launch {
            val currentItems = _listState.value.items
            val target = currentItems.firstOrNull { it.poi.id == poiId } ?: return@launch
            val newValue = !target.isFavorite

            runCatching {
                if (target.isFavorite) {
                    favoriteRepository.removeFromFavorites(poiId)
                } else {
                    favoriteRepository.addToFavorites(poiId)
                }
            }.onSuccess {
                updateFavoriteInList(poiId, newValue)
                updateFavoriteInDetails(poiId, newValue)
            }.onFailure { e ->
                _listState.value = _listState.value.copy(
                    errorMessage = e.toUserMessage("Не удалось обновить избранное")
                )
            }
        }
    }

    private suspend fun buildPoiCardItems(pois: List<com.travelguide.domain.models.POI>): List<PoiCardUiModel> =
        coroutineScope {
            pois.map { poi ->
                async {
                    val isFavorite = runCatching {
                        favoriteRepository.isFavorite(poi.id)
                    }.getOrDefault(false)

                    val stats = runCatching {
                        reviewRepository.getPoiStats(poi.id)
                    }.getOrNull()

                    PoiCardUiModel(
                        poi = poi,
                        isFavorite = isFavorite,
                        averageRating = stats?.averageRating,
                        reviewCount = stats?.totalReviews ?: 0
                    )
                }
            }.awaitAll()
        }

    private fun updateFavoriteInList(poiId: Int, isFavorite: Boolean) {
        _listState.value = _listState.value.copy(
            items = _listState.value.items.map { item ->
                if (item.poi.id == poiId) item.copy(isFavorite = isFavorite) else item
            }
        )
    }

    private fun updateFavoriteInDetails(poiId: Int, isFavorite: Boolean) {
        val currentPoi = _detailsState.value.poi
        if (currentPoi?.id == poiId) {
            _detailsState.value = _detailsState.value.copy(isFavorite = isFavorite)
        }
    }
}
