package com.humanperformcenter.shared.presentation.ui

sealed class AssignEvent {
    data class Success(val productId: Int) : AssignEvent()
    data class Error(val message: String) : AssignEvent()
}