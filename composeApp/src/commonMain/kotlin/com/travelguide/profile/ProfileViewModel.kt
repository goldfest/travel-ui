package com.travelguide.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.auth.AuthRepository
import com.travelguide.core.toUserMessage
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

    fun loadMe(force: Boolean = false) {
        val s = _state.value
        if (s.isLoggingOut || s.isLoading) return
        if (!force && s.hasLoadedOnce && s.user != null) return

        loadJob?.cancel()
        _state.update { it.copy(isLoading = true, error = null) }

        loadJob = viewModelScope.launch {
            try {
                val me = userRepo.getMe()
                _state.update {
                    it.copy(isLoading = false, user = me, error = null, hasLoadedOnce = true)
                }
            } catch (e: UnauthorizedException) {
                if (_state.value.isLoggingOut) return@launch
                _state.update { it.copy(isLoading = false, user = null, error = null, hasLoadedOnce = true) }
                authRepo.logout()
                sessionManager.unauthorized()
            } catch (e: Exception) {
                val cached = userRepo.getCachedMe()
                if (cached != null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            user = cached,
                            error = "Проверьте подключение к интернету.",
                            hasLoadedOnce = true
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.toUserMessage(),
                            hasLoadedOnce = true
                        )
                    }
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
        if (_state.value.isLoggingOut || _state.value.user == null) return

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
                _editState.update { it.copy(isSaving = false, error = null) }
                authRepo.logout()
                sessionManager.unauthorized()
            } catch (e: Exception) {
                val updatedLocal = userRepo.updateCachedProfile(username, phone, avatarUrl, homeCityId)
                if (updatedLocal != null) {
                    _state.update { it.copy(user = updatedLocal) }
                    _editState.update { it.copy(isSaving = false, error = null) }
                    snack("Изменения сохранены локально")
                    onSuccess()
                } else {
                    _editState.update { it.copy(isSaving = false, error = e.toUserMessage()) }
                }
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        _state.update { it.copy(isLoggingOut = true, error = null, isLoading = false) }
        _editState.update { it.copy(isSaving = false, error = null) }
        loadJob?.cancel()
        loadJob = null

        viewModelScope.launch {
            try {
                authRepo.logout()
            } finally {
                _state.value = ProfileUiState(isLoggingOut = true, hasLoadedOnce = true)
                _editState.value = com.travelguide.ui.screens.profile.EditProfileUiState()
                onDone()
            }
        }
    }

    fun changePassword(current: String, new: String, onSuccess: () -> Unit) {
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
                snack(e.toUserMessage())
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
                snack(e.toUserMessage())
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
                snack(e.toUserMessage("Загрузка аватара недоступна без интернета."))
            }
        }
    }
}
