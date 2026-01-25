package com.humanperformcenter.shared.presentation.ui

sealed class BookingEvent {
    data class Success(val message: String) : BookingEvent()
    data class Error(val message: String) : BookingEvent()
}