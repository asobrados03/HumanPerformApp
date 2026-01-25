package com.humanperformcenter.shared.presentation.ui

sealed class DeleteUserState {
    object Idle    : DeleteUserState()
    object Loading : DeleteUserState()
    object Success : DeleteUserState()
    data class NotFound(val email: String) : DeleteUserState()
    data class Error(val message: String) : DeleteUserState()
}