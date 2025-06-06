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
import com.humanperformcenter.shared.session.SessionManager
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

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result: Result<LoginResponse> = authUseCase.login(email, password)

            // Si es éxito: guardar en SessionManager y emitir Success
            result.onSuccess { loginResponse ->
                // 1) Guardamos el LoginResponse en memoria
                SessionManager.storeUser(loginResponse)
                // 2) Emitimos el estado de éxito, con todo el objeto
                _loginState.value = LoginState.Success(loginResponse)
            }.onFailure { throwable ->
                // Emitir el estado de error con el mensaje
                val errorMsg = throwable.message ?: "Error desconocido"
                _loginState.value = LoginState.Error(errorMsg)
            }
        }
    }

    fun register(data: RegisterRequest) {
        // 1) Ejecutar validación local antes de llamar al caso de uso
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
            // 2) Si hay errores, emitimos ValidationErrors en lugar de Loading
            _registerState.value = RegisterState.ValidationErrors(validation.fieldErrors)
            return
        }

        // 3) Si validación OK, llamamos al caso de uso para hacer la petición
        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            val result = authUseCase.register(data)
            _registerState.value = result
                .map { RegisterState.Success(it) }
                .getOrElse { RegisterState.Error(it.message ?: "Registro fallido") }
        }
    }

    fun resetStates() {
        _loginState.value = LoginState.Idle
        _registerState.value = RegisterState.Idle
    }
}
