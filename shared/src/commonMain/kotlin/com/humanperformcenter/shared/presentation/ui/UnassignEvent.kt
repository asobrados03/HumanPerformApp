package com.humanperformcenter.shared.presentation.ui

sealed class UnassignEvent {
    data object Success : UnassignEvent()
    data class Error(val message: String) : UnassignEvent()
}