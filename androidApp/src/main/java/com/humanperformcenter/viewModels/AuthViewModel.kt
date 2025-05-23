package com.humanperformcenter.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = authUseCase.login(email, password)
            _loginState.value = result
                .map { LoginState.Success(it) }
                .getOrElse { LoginState.Error(it.message ?: "Error desconocido") }
        }
    }

    fun reset() {
        _loginState.value = LoginState.Idle
    }
}