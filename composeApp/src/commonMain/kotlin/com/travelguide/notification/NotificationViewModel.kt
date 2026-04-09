package com.travelguide.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelguide.core.toUserMessage
import com.travelguide.domain.models.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val unreadOnly: Boolean = false,
    val errorMessage: String? = null,
    val unreadCount: Int = 0
)

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                val notifications = if (_state.value.unreadOnly) repository.getUnreadNotifications() else repository.getNotifications()
                val unreadCount = repository.getUnreadCount()
                notifications to unreadCount
            }.onSuccess { (notifications, unreadCount) ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    notifications = notifications,
                    unreadCount = unreadCount
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Не удалось загрузить уведомления")
                )
            }
        }
    }

    fun toggleUnreadOnly(value: Boolean) {
        _state.value = _state.value.copy(unreadOnly = value)
        load()
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            runCatching { repository.markAsRead(notificationId) }
                .onSuccess { updated ->
                    _state.value = _state.value.copy(
                        notifications = _state.value.notifications.map { if (it.id == updated.id) updated else it },
                        unreadCount = (_state.value.unreadCount - 1).coerceAtLeast(0)
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(errorMessage = e.toUserMessage("Не удалось отметить уведомление как прочитанное"))
                }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            runCatching { repository.markAllAsRead() }
                .onSuccess {
                    _state.value = _state.value.copy(
                        notifications = _state.value.notifications.map { it.copy(isRead = true) },
                        unreadCount = 0
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(errorMessage = e.toUserMessage("Не удалось отметить все уведомления как прочитанные"))
                }
        }
    }

    fun deleteNotification(notificationId: Int) {
        viewModelScope.launch {
            val target = _state.value.notifications.firstOrNull { it.id == notificationId }
            runCatching { repository.deleteNotification(notificationId) }
                .onSuccess {
                    _state.value = _state.value.copy(
                        notifications = _state.value.notifications.filterNot { it.id == notificationId },
                        unreadCount = if (target?.isRead == false) (_state.value.unreadCount - 1).coerceAtLeast(0) else _state.value.unreadCount
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(errorMessage = e.toUserMessage("Не удалось удалить уведомление"))
                }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            runCatching { repository.deleteAllNotifications() }
                .onSuccess {
                    _state.value = _state.value.copy(notifications = emptyList(), unreadCount = 0)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(errorMessage = e.toUserMessage("Не удалось удалить уведомления"))
                }
        }
    }
}
