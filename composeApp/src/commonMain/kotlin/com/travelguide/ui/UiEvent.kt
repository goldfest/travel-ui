package com.travelguide.ui

sealed interface UiEvent {
    data class Snackbar(val message: String) : UiEvent
}