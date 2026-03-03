package com.travelguide.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.travelguide.network.UnauthorizedException
import com.travelguide.session.SessionManager
import com.travelguide.ui.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class ProfileViewModel(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    private val _editState = MutableStateFlow(com.travelguide.ui.screens.profile.EditProfileUiState())
    val editState: StateFlow<com.travelguide.ui.screens.profile.EditProfileUiState> = _editState

    fun loadMe() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val me = userRepo.getMe()
                _state.update { it.copy(isLoading = false, user = me) }
            } catch (e: UnauthorizedException) {
                _state.update { it.copy(isLoading = false, error = null, user = null) }
                authRepo.logout()
                sessionManager.unauthorized()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Load profile error") }
            }
        }
    }

    fun refresh() = loadMe()

    fun saveProfile(
        username: String,
        phone: String?,
        avatarUrl: String?,
        homeCityId: Long?,
        onSuccess: () -> Unit
    ) {
        val current = _state.value.user ?: return
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
            } catch (e: Exception) {
                _editState.update { it.copy(isSaving = false, error = e.message ?: "Save error") }
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepo.logout()
            _state.update { ProfileUiState() }
            _editState.update { com.travelguide.ui.screens.profile.EditProfileUiState() }
            onDone()
        }
    }

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private suspend fun snack(msg: String) {
        _events.send(UiEvent.Snackbar(msg))
    }

    fun changePassword(
        current: String,
        new: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _editState.update { it.copy(isSaving = true, error = null) }
                userRepo.changePassword(current, new)
                _editState.update { it.copy(isSaving = false) }
                snack("Пароль успешно изменён")
                onSuccess()
            } catch (e: UnauthorizedException) {
                authRepo.logout()
                sessionManager.unauthorized()
            } catch (e: Exception) {
                _editState.update { it.copy(isSaving = false) }
                snack(e.message ?: "Ошибка смены пароля")
            }
        }
    }

    fun deleteAccount(
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _editState.update { it.copy(isSaving = true, error = null) }
                userRepo.deleteMe()
                authRepo.logout()
                _editState.update { it.copy(isSaving = false) }
                snack("Аккаунт удалён")
                onSuccess()
            } catch (e: UnauthorizedException) {
                authRepo.logout()
                sessionManager.unauthorized()
            } catch (e: Exception) {
                _editState.update { it.copy(isSaving = false) }
                snack(e.message ?: "Ошибка удаления аккаунта")
            }
        }
    }

    fun uploadAvatar(
        bytes: ByteArray,
        mimeType: String
    ) {
        viewModelScope.launch {
            try {
                _editState.update { it.copy(isSaving = true, error = null) }
                val updated = userRepo.uploadAvatar(bytes, mimeType)
                _state.update { it.copy(user = updated) }
                _editState.update { it.copy(isSaving = false) }
                snack("Аватар обновлён")
            } catch (e: UnauthorizedException) {
                authRepo.logout()
                sessionManager.unauthorized()
            } catch (e: Exception) {
                _editState.update { it.copy(isSaving = false) }
                snack(e.message ?: "Ошибка загрузки аватара")
            }
        }
    }
}