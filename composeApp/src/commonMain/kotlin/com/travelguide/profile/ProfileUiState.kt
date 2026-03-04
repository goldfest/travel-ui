package com.travelguide.profile

import com.travelguide.domain.models.User

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,

    // чтобы при logout/unauthorized не показывать ошибки/мигания
    val isLoggingOut: Boolean = false,

    // чтобы не дергать loadMe() заново при каждом входе/перерисовке
    val hasLoadedOnce: Boolean = false
)