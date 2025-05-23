package com.humanperformcenter.viewModels

import com.humanperformcenter.shared.data.model.LoginResponse

sealed class LoginState {
    object Idle    : LoginState()
    object Loading : LoginState()
    data class Success(val user: LoginResponse) : LoginState()
    data class Error  (val message: String)     : LoginState()
}