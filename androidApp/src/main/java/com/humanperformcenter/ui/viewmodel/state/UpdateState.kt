package com.humanperformcenter.ui.viewmodel.state

import com.humanperformcenter.shared.data.model.LoginResponse

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    data class Success(val updatedUser: LoginResponse) : UpdateState()
    data class Error(val message: String) : UpdateState()
}