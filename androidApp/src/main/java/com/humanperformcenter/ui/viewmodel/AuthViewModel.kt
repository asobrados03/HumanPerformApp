package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.data.model.RegisterRequest
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.domain.usecase.validation.RegisterValidationResult
import com.humanperformcenter.shared.domain.usecase.validation.UserValidator
import com.humanperformcenter.ui.viewmodel.state.ChangePasswordState
import com.humanperformcenter.ui.viewmodel.state.LoginState
import com.humanperformcenter.ui.viewmodel.state.RegisterState
import kotlinx.coroutines.launch
import kotlin.onSuccess

class AuthViewModel(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    private val _registerState = MutableLiveData<RegisterState>(RegisterState.Idle)
    val registerState: LiveData<RegisterState> = _registerState

    private val _isChangingPassword = MutableLiveData<ChangePasswordState>(ChangePasswordState.Idle)
    val isChangingPassword: LiveData<ChangePasswordState> = _isChangingPassword

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result: Result<LoginResponse> = authUseCase.login(email, password)

            result.onSuccess { loginResponse ->
                _loginState.value = LoginState.Success(loginResponse)
            }.onFailure { throwable ->
                val errorMsg = throwable.message ?: "Error desconocido"
                _loginState.value = LoginState.Error(errorMsg)
            }
        }
    }

    fun register(data: RegisterRequest) {
        val validation = UserValidator.validateRegister(
            firstName = data.nombre,
            lastName = data.apellidos,
            email = data.email,
            phone = data.telefono,
            password = data.password,
            dateOfBirthText = data.fechaNacimiento,
            selectedSexBackend = data.sexo,
            postcode = data.codigoPostal,
            dni = data.dni
        )

        if (validation is RegisterValidationResult.Error) {
            _registerState.value = RegisterState.ValidationErrors(validation.fieldErrors)
            return
        }

        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            val result = authUseCase.register(data)
            _registerState.value = result
                .map { RegisterState.Success(it) }
                .getOrElse { RegisterState.Error(it.message ?: "Registro fallido") }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        userId: Int
    ) {
        _isChangingPassword.value = ChangePasswordState.Loading

        viewModelScope.launch {
            val result = authUseCase.changePassword(currentPassword, newPassword, confirmPassword, userId)
            _isChangingPassword.value = result
                .map { ChangePasswordState.Success("Contraseña cambiada exitosamente") }
                .getOrElse { ChangePasswordState.Error(it.message ?: "Cambio de contraseña fallido") }
        }
    }

    fun resetStates() {
        _loginState.value = LoginState.Idle
        _registerState.value = RegisterState.Idle
    }

    fun resetChangePasswordState() {
        _isChangingPassword.value = ChangePasswordState.Idle
    }
}
