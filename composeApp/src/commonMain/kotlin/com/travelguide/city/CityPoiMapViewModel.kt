package com.travelguide.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.core.toUserMessage
import com.travelguide.domain.models.City
import com.travelguide.domain.models.POI
import com.travelguide.domain.models.POIType
import com.travelguide.poi.PoiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CityPoiMapUiState(
    val isLoading: Boolean = false,
    val city: City? = null,
    val poiTypes: List<POIType> = emptyList(),
    val selectedTypeCode: String? = null,
    val pois: List<POI> = emptyList(),
    val errorMessage: String? = null
)

class CityPoiMapViewModel(
    private val cityRepository: CityRepository,
    private val poiRepository: PoiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CityPoiMapUiState())
    val state: StateFlow<CityPoiMapUiState> = _state.asStateFlow()

    fun load(cityId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            runCatching {
                val city = cityRepository.getCityById(cityId)
                val pois = poiRepository.getPoisByCity(cityId)
                val types = poiRepository.getPoiTypes()
                CityPoiMapUiState(
                    isLoading = false,
                    city = city,
                    poiTypes = types,
                    pois = pois,
                    selectedTypeCode = null,
                    errorMessage = null
                )
            }.onSuccess {
                _state.value = it
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Не удалось загрузить карту объектов")
                )
            }
        }
    }

    fun selectType(code: String?) {
        _state.value = _state.value.copy(selectedTypeCode = code)
    }
}