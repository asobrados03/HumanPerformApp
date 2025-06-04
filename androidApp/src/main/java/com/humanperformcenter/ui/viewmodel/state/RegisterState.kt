package com.humanperformcenter.ui.viewmodel.state

import com.humanperformcenter.shared.data.model.RegisterResponse

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: RegisterResponse) : RegisterState()
    data class Error(val message: String) : RegisterState()
}
