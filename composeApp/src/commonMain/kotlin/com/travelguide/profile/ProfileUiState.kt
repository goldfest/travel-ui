package com.travelguide.profile

import com.travelguide.domain.models.User

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null
)