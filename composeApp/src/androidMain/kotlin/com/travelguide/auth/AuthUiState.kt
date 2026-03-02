package com.travelguide.auth

import com.travelguide.domain.models.User

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null
)