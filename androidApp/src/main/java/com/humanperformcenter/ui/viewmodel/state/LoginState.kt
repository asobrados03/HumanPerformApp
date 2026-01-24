package com.humanperformcenter.ui.viewmodel.state

import com.humanperformcenter.shared.data.model.auth.LoginResponse

sealed class LoginState {
    object Idle    : LoginState()
    object Loading : LoginState()
    data class Success(val user: LoginResponse) : LoginState()
    data class Error  (val message: String)     : LoginState()
}