package com.travelguide.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

sealed interface SessionEvent {
    data class Unauthorized(val message: String = "Сессия истекла. Войдите снова.") : SessionEvent
}

class SessionManager {
    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<SessionEvent> = _events

    fun unauthorized(message: String = "Сессия истекла. Войдите снова.") {
        _events.tryEmit(SessionEvent.Unauthorized(message))
    }
}