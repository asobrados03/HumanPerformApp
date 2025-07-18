package com.humanperformcenter.ui.viewmodel.state

sealed class DeleteProfilePicState {
    object Idle    : DeleteProfilePicState()
    object Loading : DeleteProfilePicState()
    object Success : DeleteProfilePicState()
    data class Error(val message: String) : DeleteProfilePicState()
}