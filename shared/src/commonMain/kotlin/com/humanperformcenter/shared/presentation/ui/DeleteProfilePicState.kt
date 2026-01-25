package com.humanperformcenter.shared.presentation.ui

sealed class DeleteProfilePicState {
    object Idle    : DeleteProfilePicState()
    object Loading : DeleteProfilePicState()
    object Success : DeleteProfilePicState()
    data class Error(val message: String) : DeleteProfilePicState()
}