package com.humanperformcenter.shared.presentation.viewmodel

import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.domain.usecase.validation.RegisterValidationResult
import com.humanperformcenter.shared.domain.usecase.validation.UserValidator
import com.humanperformcenter.shared.presentation.ui.ChangePasswordState
import com.humanperformcenter.shared.presentation.ui.LoginState
import com.humanperformcenter.shared.presentation.ui.RegisterState
import com.humanperformcenter.shared.presentation.ui.ResetPasswordState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    // Usamos MutableStateFlow con un valor inicial obligatorio
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _isChangingPassword = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val isChangingPassword: StateFlow<ChangePasswordState> = _isChangingPassword.asStateFlow()

    private val _isResettingPassword = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val isResettingPassword: StateFlow<ResetPasswordState> = _isResettingPassword.asStateFlow()

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = authUseCase.login(email, password)

            // StateFlow permite actualizar .value desde cualquier hilo (Dispatchers.IO es seguro aquí)
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
            firstName = data.name,
            lastName = data.surnames,
            email = data.email,
            phone = data.phone,
            password = data.password,
            dateOfBirthText = data.dateOfBirth,
            selectedSexBackend = data.sex,
            postcode = data.postCode,
            address = data.postAddress,
            dni = data.dni
        )

        if (validation is RegisterValidationResult.Error) {
            _registerState.value = RegisterState.ValidationErrors(validation.fieldErrors)
            return
        }

        _registerState.value = RegisterState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = authUseCase.register(data)
            _registerState.value = result
                .map { RegisterState.Success(it) }
                .getOrElse { RegisterState.Error(it.message ?: "Registro fallido") }
        }
    }

    fun resetPassword(email: String) {
        _isResettingPassword.value = ResetPasswordState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = authUseCase.resetPassword(email)
            _isResettingPassword.value = result
                .map { ResetPasswordState.Success("Contraseña restablecida exitosamente") }
                .getOrElse { ResetPasswordState.Error(it.message ?: "Error al restablecer") }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        userId: Int
    ) {
        _isChangingPassword.value = ChangePasswordState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = authUseCase.changePassword(currentPassword, newPassword, confirmPassword, userId)
            _isChangingPassword.value = result
                .map { ChangePasswordState.Success("Contraseña cambiada exitosamente") }
                .getOrElse { ChangePasswordState.Error(it.message ?: "Error al cambiar contraseña") }
        }
    }

    fun resetStates() {
        _loginState.value = LoginState.Idle
        _registerState.value = RegisterState.Idle
    }

    fun resetChangePasswordState() {
        _isChangingPassword.value = ChangePasswordState.Idle
    }

    fun resetResettingPasswordState() {
        _isResettingPassword.value = ResetPasswordState.Idle
    }
}
