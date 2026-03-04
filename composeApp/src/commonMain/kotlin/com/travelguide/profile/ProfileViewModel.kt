package com.travelguide.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.auth.AuthRepository
import com.travelguide.network.UnauthorizedException
import com.travelguide.session.SessionManager
import com.travelguide.ui.UiEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    private val _editState = MutableStateFlow(com.travelguide.ui.screens.profile.EditProfileUiState())
    val editState: StateFlow<com.travelguide.ui.screens.profile.EditProfileUiState> = _editState

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var loadJob: Job? = null

    private suspend fun snack(msg: String) {
        _events.send(UiEvent.Snackbar(msg))
    }

    /**
     * Вызывай при входе на экран профиля:
     * - один раз
     * - или при pull-to-refresh
     */
    fun loadMe(force: Boolean = false) {
        val s = _state.value
        if (s.isLoggingOut) return
        if (s.isLoading) return
        if (!force && s.hasLoadedOnce && s.user != null) return

        loadJob?.cancel()
        _state.update { it.copy(isLoading = true, error = null) }

        loadJob = viewModelScope.launch {
            try {
                val me = userRepo.getMe()
                _state.update {
                    it.copy(
                        isLoading = false,
                        user = me,
                        error = null,
                        hasLoadedOnce = true
                    )
                }
            } catch (e: UnauthorizedException) {
                // если уже выходим — молча игнорируем
                if (_state.value.isLoggingOut) return@launch

                // не показываем "ошибка загрузки", потому что это просто неавторизован
                _state.update {
                    it.copy(
                        isLoading = false,
                        user = null,
                        error = null,
                        hasLoadedOnce = true
                    )
                }

                // здесь нормально переводить на авторизацию
                authRepo.logout()
                sessionManager.unauthorized()
            } catch (e: Exception) {
                if (_state.value.isLoggingOut) return@launch
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Load profile error",
                        hasLoadedOnce = true
                    )
                }
            }
        }
    }

    fun refresh() = loadMe(force = true)

    fun saveProfile(
        username: String,
        phone: String?,
        avatarUrl: String?,
        homeCityId: Long?,
        onSuccess: () -> Unit
    ) {
        if (_state.value.isLoggingOut) return
        if (_state.value.user == null) return

        _editState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val updated = userRepo.updateMe(
                    username = username,
                    phone = phone,
                    avatarUrl = avatarUrl,
                    homeCityId = homeCityId
                )
                _state.update { it.copy(user = updated) }
                _editState.update { it.copy(isSaving = false, error = null) }
                onSuccess()
            } catch (e: UnauthorizedException) {
                // при 401 не показываем "ошибка сохранения", просто уходим в auth
                _editState.update { it.copy(isSaving = false, error = null) }
                authRepo.logout()
                sessionManager.unauthorized()
            } catch (e: Exception) {
                _editState.update { it.copy(isSaving = false, error = e.message ?: "Save error") }
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        // важно: сразу гасим любые загрузки/ошибки, чтобы UI не успел моргнуть
        _state.update { it.copy(isLoggingOut = true, error = null, isLoading = false) }
        _editState.update { it.copy(isSaving = false, error = null) }

        // отменяем активную загрузку профиля
        loadJob?.cancel()
        loadJob = null

        viewModelScope.launch {
            try {
                authRepo.logout()
            } finally {
                // чистим стейты и уходим
                _state.value = ProfileUiState(isLoggingOut = true, hasLoadedOnce = true)
                _editState.value = com.travelguide.ui.screens.profile.EditProfileUiState()
                onDone()
            }
        }
    }

    fun changePassword(
        current: String,
        new: String,
        onSuccess: () -> Unit
    ) {
        if (_state.value.isLoggingOut) return

        viewModelScope.launch {
            try {
                _editState.update { it.copy(isSaving = true, error = null) }
                userRepo.changePassword(current, new)
                _editState.update { it.copy(isSaving = false, error = null) }
                snack("Пароль успешно изменён")
                onSuccess()
            } catch (e: UnauthorizedException) {
                _editState.update { it.copy(isSaving = false, error = null) }
                authRepo.logout()
                sessionManager.unauthorized()
            } catch (e: Exception) {
                _editState.update { it.copy(isSaving = false) }
                snack(e.message ?: "Ошибка смены пароля")
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        if (_state.value.isLoggingOut) return

        viewModelScope.launch {
            try {
                _editState.update { it.copy(isSaving = true, error = null) }
                userRepo.deleteMe()
                authRepo.logout()
                _editState.update { it.copy(isSaving = false, error = null) }
                snack("Аккаунт удалён")
                onSuccess()
            } catch (e: UnauthorizedException) {
                _editState.update { it.copy(isSaving = false, error = null) }
                authRepo.logout()
                sessionManager.unauthorized()
            } catch (e: Exception) {
                _editState.update { it.copy(isSaving = false) }
                snack(e.message ?: "Ошибка удаления аккаунта")
            }
        }
    }

    fun uploadAvatar(bytes: ByteArray, mimeType: String) {
        if (_state.value.isLoggingOut) return

        viewModelScope.launch {
            try {
                _editState.update { it.copy(isSaving = true, error = null) }
                val updated = userRepo.uploadAvatar(bytes, mimeType)
                _state.update { it.copy(user = updated) }
                _editState.update { it.copy(isSaving = false, error = null) }
                snack("Аватар обновлён")
            } catch (e: UnauthorizedException) {
                _editState.update { it.copy(isSaving = false, error = null) }
                authRepo.logout()
                sessionManager.unauthorized()
            } catch (e: Exception) {
                _editState.update { it.copy(isSaving = false) }
                snack(e.message ?: "Ошибка загрузки аватара")
            }
        }
    }
}