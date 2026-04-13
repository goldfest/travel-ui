package com.travelguide.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.city.CityRepository
import com.travelguide.domain.models.City
import com.travelguide.domain.models.POI
import com.travelguide.domain.models.Route
import com.travelguide.domain.models.TransportMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged

import com.travelguide.core.isInternetUnavailableIssue
import com.travelguide.core.isServerUnavailableIssue
import com.travelguide.core.toUserMessage
class RouteEditorViewModel(
    private val routeRepository: RouteRepository,
    private val cityRepository: CityRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RouteEditorUiState())
    val state: StateFlow<RouteEditorUiState> = _state.asStateFlow()

    private var originalRoute: Route? = null
    private var pendingSyncJob: Job? = null
    private var draftSaveJob: Job? = null
    private var currentDraftKey: String? = null

    fun startCreate(cityId: Int?, initialPoiId: Int?) {
        currentDraftKey = buildCreateDraftKey(cityId, initialPoiId)
        viewModelScope.launch {
            _state.value = RouteEditorUiState(
                isLoading = true,
                mode = RouteEditorMode.CREATE,
                selectedCityId = cityId
            )

            runCatching {
                val cities = cityRepository.getCities(size = 100)
                val selectedCity = cityId?.let { id -> cities.firstOrNull { it.id == id } }
                val pois = cityId?.let { routeRepository.getPoisForRouteCreation(it) }.orEmpty()

                Triple(cities, selectedCity, pois)
            }.onSuccess { (cities, selectedCity, pois) ->
                val draft = currentDraftKey?.let { routeRepository.getEditorDraft(it) }
                _state.value = _state.value.copy(
                    isLoading = false,
                    availableCities = cities,
                    selectedCityId = selectedCity?.id,
                    selectedCityName = selectedCity?.displayName().orEmpty(),
                    availablePois = pois,
                    filteredPois = filterPois(pois, _state.value.searchQuery),
                    errorMessage = null,
                    syncNoticeMessage = draft?.let { "Восстановлен локальный черновик маршрута." }
                ).let { base ->
                    if (draft != null) base.applyDraft(draft, pois) else base
                }

                _state.value.selectedCityId?.let { refreshGraphState(it) }

                if (draft == null && initialPoiId != null) {
                    preselectPoiIfNeeded(initialPoiId)
                }
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Не удалось подготовить создание маршрута")
                )
            }
        }
    }

    fun startEdit(routeId: Int) {
        currentDraftKey = buildEditDraftKey(routeId)
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                mode = RouteEditorMode.EDIT,
                routeId = routeId
            )

            runCatching {
                val route = routeRepository.getRouteById(routeId)
                val cities = runCatching { cityRepository.getCities(size = 100) }.getOrDefault(emptyList())
                val city = cities.firstOrNull { it.id == route.cityId }
                    ?: runCatching { cityRepository.getCityById(route.cityId) }.getOrNull()
                val pois = runCatching { routeRepository.getPoisForRouteCreation(route.cityId) }
                    .getOrDefault(
                        route.days
                            .flatMap { day -> day.points }
                            .mapNotNull { point -> point.poi }
                            .distinctBy { poi -> poi.id }
                    )
                EditorBootstrap(route, cities, city, pois)
            }.onSuccess { data ->
                originalRoute = data.route
                val draft = currentDraftKey?.let { routeRepository.getEditorDraft(it) }

                val mappedDays = data.route.days
                    .sortedBy { it.dayNumber }
                    .map { day ->
                        EditableRouteDayUi(
                            routeDayId = day.id,
                            dayNumber = day.dayNumber,
                            description = day.description.orEmpty(),
                            points = day.points
                                .sortedBy { it.orderIndex }
                                .map { point ->
                                    EditableRoutePointUi(
                                        routePointId = point.id,
                                        poi = data.pois.firstOrNull { it.id == point.poiId }
                                            ?: point.toEditorPoi(data.route.cityId),
                                        estimatedVisitMinutes = point.estimatedVisitMinutes
                                    )
                                }
                        )
                    }
                    .ifEmpty { listOf(EditableRouteDayUi(dayNumber = 1)) }

                val offlineEditNotice = if (data.cities.isEmpty()) {
                    "Вы редактируете маршрут без интернета. Изменения сохранятся локально и будут отправлены на сервер после появления сети."
                } else {
                    null
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    routeId = data.route.id,
                    availableCities = data.cities,
                    selectedCityId = data.route.cityId,
                    selectedCityName = data.city?.displayName() ?: "Город #${data.route.cityId}",
                    routeName = data.route.name,
                    routeDescription = data.route.description.orEmpty(),
                    selectedTransport = data.route.transportMode,
                    availablePois = data.pois,
                    filteredPois = filterPois(data.pois, _state.value.searchQuery),
                    days = mappedDays,
                    selectedDayNumber = mappedDays.first().dayNumber,
                    isGraphReady = true,
                    isGraphLoading = false,
                    errorMessage = null,
                    syncNoticeMessage = draft?.let { "Восстановлен локальный черновик изменений маршрута." } ?: offlineEditNotice
                ).let { base ->
                    if (draft != null) base.applyDraft(draft, data.pois) else base
                }

                observePendingSync(data.route.id)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Не удалось загрузить маршрут")
                )
            }
        }
    }

    fun onNameChange(value: String) {
        updateState { copy(routeName = value) }
    }

    fun onDescriptionChange(value: String) {
        updateState { copy(routeDescription = value) }
    }

    fun onTransportChange(value: TransportMode) {
        updateState { copy(selectedTransport = value) }
    }

    fun onSearchChange(value: String) {
        updateState {
            copy(
                searchQuery = value,
                filteredPois = filterPois(availablePois, value)
            )
        }
    }

    fun onCitySelected(cityId: Int) {
        viewModelScope.launch {
            val current = _state.value
            if (current.mode == RouteEditorMode.EDIT) return@launch
            if (current.selectedCityId == cityId) return@launch

            _state.value = current.copy(isLoading = true, errorMessage = null)

            runCatching {
                val city = current.availableCities.firstOrNull { it.id == cityId }
                    ?: cityRepository.getCityById(cityId)
                val pois = routeRepository.getPoisForRouteCreation(cityId)
                city to pois
            }.onSuccess { (city, pois) ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    selectedCityId = city.id,
                    selectedCityName = city.displayName(),
                    availablePois = pois,
                    filteredPois = filterPois(pois, _state.value.searchQuery),
                    days = listOf(EditableRouteDayUi(dayNumber = 1)),
                    selectedDayNumber = 1,
                    isGraphReady = false,
                    isGraphLoading = true,
                    isGraphDownloadInProgress = false,
                    graphMessage = null,
                    errorMessage = null,
                    syncNoticeMessage = null
                )
                scheduleDraftSave(immediate = true)
                refreshGraphState(city.id)
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Не удалось выбрать город")
                )
            }
        }
    }


    fun downloadGraphForSelectedCity() {
        val cityId = _state.value.selectedCityId ?: return
        if (_state.value.mode == RouteEditorMode.EDIT) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isGraphDownloadInProgress = true,
                isGraphLoading = true,
                graphMessage = "Подготавливаем граф города…",
                errorMessage = null
            )

            runCatching {
                routeRepository.requestCityGraphDownload(cityId)
                repeat(30) {
                    if (routeRepository.isCityGraphReady(cityId)) {
                        return@runCatching true
                    }
                    delay(2000)
                }
                false
            }.onSuccess { ready ->
                _state.value = _state.value.copy(
                    isGraphDownloadInProgress = false,
                    isGraphLoading = false,
                    isGraphReady = ready,
                    graphMessage = if (ready) {
                        "Граф дорог готов. Можно создавать маршрут."
                    } else {
                        "Граф ещё готовится. Нажмите кнопку позже, чтобы обновить статус."
                    }
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isGraphDownloadInProgress = false,
                    isGraphLoading = false,
                    errorMessage = e.toUserMessage("Не удалось скачать граф города")
                )
            }
        }
    }

    private fun refreshGraphState(cityId: Int) {
        if (_state.value.mode == RouteEditorMode.EDIT) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isGraphLoading = true, graphMessage = null)
            runCatching { routeRepository.isCityGraphReady(cityId) }
                .onSuccess { ready ->
                    _state.value = _state.value.copy(
                        isGraphLoading = false,
                        isGraphReady = ready,
                        graphMessage = if (ready) {
                            "Граф дорог уже скачан."
                        } else {
                            "Перед созданием маршрута нужно скачать граф города."
                        }
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        isGraphLoading = false,
                        isGraphReady = false,
                        graphMessage = "Не удалось проверить граф. Для создания маршрута потребуется повторная проверка."
                    )
                }
        }
    }

    fun selectDay(dayNumber: Int) {
        updateState { copy(selectedDayNumber = dayNumber) }
    }

    fun addDay() {
        val current = _state.value
        if (current.mode == RouteEditorMode.EDIT) {
            _state.value = current.copy(
                errorMessage = "Добавление новых дней пока не поддерживается сервером в режиме редактирования"
            )
            return
        }

        val nextDayNumber = (current.days.maxOfOrNull { it.dayNumber } ?: 0) + 1

        updateState {
            copy(
                days = current.days + EditableRouteDayUi(dayNumber = nextDayNumber),
                selectedDayNumber = nextDayNumber
            )
        }
    }

    fun removeDay(dayNumber: Int) {
        val current = _state.value
        if (current.days.size <= 1) return

        if (current.mode == RouteEditorMode.EDIT) {
            _state.value = current.copy(
                errorMessage = "Удаление дней пока не поддерживается сервером в режиме редактирования"
            )
            return
        }

        val updated = current.days
            .filterNot { it.dayNumber == dayNumber }
            .mapIndexed { index, day -> day.copy(dayNumber = index + 1) }

        updateState {
            copy(
                days = updated,
                selectedDayNumber = updated.firstOrNull()?.dayNumber ?: 1
            )
        }
    }

    fun onDayDescriptionChange(dayNumber: Int, value: String) {
        updateState {
            copy(
                days = days.map { day ->
                    if (day.dayNumber == dayNumber) day.copy(description = value) else day
                }
            )
        }
    }

    fun addPoiToSelectedDay(poi: POI) {
        val current = _state.value
        val selectedDayNumber = current.selectedDayNumber

        val updatedDays = current.days.map { day ->
            if (day.dayNumber != selectedDayNumber) return@map day
            if (day.points.any { it.poi.id == poi.id }) return@map day

            day.copy(
                points = day.points + EditableRoutePointUi(poi = poi)
            )
        }

        updateState { copy(days = updatedDays) }
    }

    fun removePoiFromDay(dayNumber: Int, poiId: Int) {
        val current = _state.value

        val updatedDays = current.days.map { day ->
            if (day.dayNumber != dayNumber) return@map day
            day.copy(points = day.points.filterNot { it.poi.id == poiId })
        }

        updateState { copy(days = updatedDays) }
    }

    fun movePoint(dayNumber: Int, fromIndex: Int, toIndex: Int) {
        val current = _state.value

        val updatedDays = current.days.map { day ->
            if (day.dayNumber != dayNumber) return@map day
            if (fromIndex !in day.points.indices || toIndex !in day.points.indices) return@map day

            val mutable = day.points.toMutableList()
            val item = mutable.removeAt(fromIndex)
            mutable.add(toIndex, item)
            day.copy(points = mutable)
        }

        updateState { copy(days = updatedDays) }
    }

    fun preselectPoiIfNeeded(poiId: Int) {
        val poi = _state.value.availablePois.firstOrNull { it.id == poiId } ?: return
        addPoiToSelectedDay(poi)
    }

    fun save() {
        val current = _state.value

        if (current.routeName.isBlank()) {
            _state.value = current.copy(errorMessage = "Введите название маршрута")
            return
        }

        val cityId = current.selectedCityId
        if (cityId == null) {
            _state.value = current.copy(errorMessage = "Выберите город")
            return
        }

        if (current.mode == RouteEditorMode.CREATE && !current.isGraphReady) {
            _state.value = current.copy(errorMessage = "Сначала скачайте граф выбранного города")
            return
        }

        if (current.days.none { it.points.isNotEmpty() }) {
            _state.value = current.copy(errorMessage = "Добавьте хотя бы одну точку")
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(isSaving = true, errorMessage = null, syncNoticeMessage = null, savedRouteId = null)

            runCatching {
                when (current.mode) {
                    RouteEditorMode.CREATE -> {
                        routeRepository.createRoute(
                            cityId = cityId,
                            name = current.routeName.trim(),
                            description = current.routeDescription.trim().ifBlank { null },
                            transportMode = current.selectedTransport,
                            days = current.days
                        )
                    }

                    RouteEditorMode.EDIT -> {
                        saveEdit(current)
                    }
                }
            }.onSuccess { route ->
                val hasPending = routeRepository.hasPendingSync(route.id)
                val notice = when {
                    route.id < 0 -> "Маршрут сохранён локально. Он будет создан на сервере автоматически после появления интернета."
                    hasPending -> "Изменения сохранены локально и будут синхронизированы после подключения к интернету."
                    else -> "Изменения успешно сохранены."
                }

                currentDraftKey?.let { routeRepository.deleteEditorDraft(it) }
                _state.value = _state.value.copy(
                    isSaving = false,
                    savedRouteId = route.id,
                    errorMessage = null,
                    syncNoticeMessage = notice
                )

                observePendingSync(route.id)
            }.onFailure { e ->
                val currentRouteId = current.routeId
                val hasPending = currentRouteId?.let { routeRepository.hasPendingSync(it) } == true
                if (hasPending && e.shouldKeepOfflineChanges()) {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        savedRouteId = null,
                        errorMessage = null,
                        syncNoticeMessage = "Изменения сохранены локально и будут синхронизированы после подключения к интернету."
                    )
                    currentRouteId?.let(::observePendingSync)
                } else {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = e.toUserMessage("Не удалось сохранить маршрут")
                    )
                }
            }
        }
    }


    fun persistDraftNow() {
        scheduleDraftSave(immediate = true)
    }

    private fun updateState(transform: RouteEditorUiState.() -> RouteEditorUiState) {
        _state.value = _state.value.transform()
        scheduleDraftSave()
    }

    private fun scheduleDraftSave(immediate: Boolean = false) {
        val draftKey = currentDraftKey ?: return
        val snapshot = _state.value
        if (snapshot.isLoading || snapshot.isSaving) return

        draftSaveJob?.cancel()
        draftSaveJob = viewModelScope.launch {
            if (!immediate) delay(500)
            routeRepository.saveEditorDraft(draftKey, _state.value)
        }
    }

    private fun observePendingSync(routeId: Int) {
        pendingSyncJob?.cancel()
        pendingSyncJob = viewModelScope.launch {
            routeRepository.observePendingSync(routeId)
                .distinctUntilChanged()
                .collect { hasPending ->
                    val currentState = _state.value
                    val existingMessage = currentState.syncNoticeMessage
                    val shouldShowPendingNotice = currentState.availableCities.isEmpty() || routeId < 0
                    val message = when {
                        hasPending && routeId < 0 -> "Маршрут сохранён локально. Он будет создан на сервере автоматически после появления интернета."
                        hasPending && shouldShowPendingNotice -> "Ваши изменения пока не применены на сервере. Подключитесь к интернету — синхронизация выполнится автоматически."
                        existingMessage?.contains("без интернета") == true -> existingMessage
                        else -> null
                    }
                    _state.value = currentState.copy(syncNoticeMessage = message)
                }
        }
    }

    private suspend fun saveEdit(current: RouteEditorUiState): Route {
        val routeId = current.routeId ?: error("routeId is null in EDIT mode")
        val original = originalRoute ?: routeRepository.getRouteById(routeId)

        if (current.days.size != original.days.size) {
            error("Сервер пока не поддерживает добавление или удаление дней при редактировании")
        }

        routeRepository.updateRouteMeta(
            routeId = routeId,
            name = current.routeName.trim(),
            description = current.routeDescription.trim().ifBlank { null },
            transportMode = current.selectedTransport
        )

        original.days.sortedBy { it.dayNumber }.forEach { originalDay ->
            val currentDay = current.days.firstOrNull { it.dayNumber == originalDay.dayNumber }
                ?: return@forEach

            val currentPointIds = currentDay.points.mapNotNull { it.routePointId }.toSet()

            originalDay.points
                .filterNot { it.id in currentPointIds }
                .forEach { removedPoint ->
                    routeRepository.removePointFromRoute(routeId, removedPoint.id)
                }

            currentDay.points
                .filter { it.routePointId == null }
                .forEachIndexed { index, newPoint ->
                    routeRepository.addPoiToRoute(
                        routeId = routeId,
                        poiId = newPoint.poi.id,
                        dayNumber = originalDay.dayNumber,
                        orderIndex = index + 1
                    )
                }
        }

        val afterAddRemove = routeRepository.getRouteById(routeId)

        afterAddRemove.days.sortedBy { it.dayNumber }.forEach { reloadedDay ->
            val desiredDay = current.days.firstOrNull { it.dayNumber == reloadedDay.dayNumber }
                ?: return@forEach

            val usedIds = mutableSetOf<Int>()
            val desiredPointIds = desiredDay.points.mapNotNull { desiredPoint ->
                val matched = reloadedDay.points.firstOrNull { actual ->
                    actual.poiId == desiredPoint.poi.id && actual.id !in usedIds
                }
                matched?.id?.also { usedIds += it }
            }

            if (desiredPointIds.size == reloadedDay.points.size && desiredPointIds.isNotEmpty()) {
                routeRepository.reorderDayPoints(
                    routeId = routeId,
                    dayId = reloadedDay.id,
                    orderedPointIds = desiredPointIds
                )
            }
        }

        val finalRoute = routeRepository.getRouteById(routeId)
        originalRoute = finalRoute
        return finalRoute
    }

    override fun onCleared() {
        draftSaveJob?.cancel()
        pendingSyncJob?.cancel()
        super.onCleared()
    }

    private fun filterPois(source: List<POI>, query: String): List<POI> {
        if (query.isBlank()) return source
        return source.filter { poi ->
            poi.name.contains(query, ignoreCase = true) ||
                    (poi.address?.contains(query, ignoreCase = true) == true)
        }
    }
}

private fun buildCreateDraftKey(cityId: Int?, initialPoiId: Int?): String =
    "route-editor:create:${cityId ?: 0}:${initialPoiId ?: 0}"

private fun buildEditDraftKey(routeId: Int): String = "route-editor:edit:$routeId"

private data class EditorBootstrap(
    val route: Route,
    val cities: List<City>,
    val city: City?,
    val pois: List<POI>
)

private fun City.displayName(): String {
    return listOfNotNull(name, country).joinToString(", ")
}

private fun com.travelguide.domain.models.RoutePoint.toEditorPoi(cityId: Int): POI {
    return POI(
        id = poiId,
        name = poiName.orEmpty(),
        description = null,
        address = poiAddress,
        latitude = poiLatitude,
        longitude = poiLongitude,
        priceLevel = null,
        cityId = cityId,
        poiTypeId = 0,
        poiType = null,
        tags = emptyList(),
        images = emptyList(),
        features = emptyList()
    )
}
private fun Throwable.shouldKeepOfflineChanges(): Boolean {
    return isInternetUnavailableIssue() || isServerUnavailableIssue()
}
