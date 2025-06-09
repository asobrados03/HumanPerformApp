package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.domain.usecase.UserUseCase
import com.humanperformcenter.shared.domain.usecase.validation.UserValidator
import com.humanperformcenter.shared.domain.usecase.validation.EditValidationResult
import com.humanperformcenter.shared.session.SessionManager
import com.humanperformcenter.ui.viewmodel.state.DeleteUserState
import com.humanperformcenter.ui.viewmodel.state.UpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(
    private val userUseCase: UserUseCase
) : ViewModel() {

    // 1) Flujo en memoria que contiene el usuario logueado
    val userData: StateFlow<LoginResponse?> = SessionManager.user
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SessionManager.getCurrentUser()
        )

    // 2) LiveData para exponer el estado de la operación de update
    private val _updateState = MutableLiveData<UpdateState>(UpdateState.Idle)
    val updateState: LiveData<UpdateState> = _updateState

    private val _deleteState = MutableStateFlow<DeleteUserState>(DeleteUserState.Idle)
    val deleteState: StateFlow<DeleteUserState> = _deleteState.asStateFlow()

    /**
     * Recibe un LoginResponse “candidato” (con campos fullName, dateOfBirth = "yyyy-MM-dd",
     * sex, phone, postcode, dni, etc.). Primero lo valida mediante el caso de uso; si hay
     * errores, emite ValidationErrors con el mapa. Si no, emite Loading y llama a updateUser()
     * del caso de uso.
     */
    fun updateUser(candidate: LoginResponse) {
        // 1) Convertir dateOfBirth de "yyyy-MM-dd" (que la UI ya le pasó) a formato dd/MM/yyyy
        //    para validar en el caso de uso. Podemos invertir la cadena:
        val dobParts = candidate.dateOfBirth.split("-")
        val dateOfBirthText = if (dobParts.size == 3) {
            val y = dobParts[0].padStart(4, '0')
            val m = dobParts[1].padStart(2, '0')
            val d = dobParts[2].padStart(2, '0')
            "$d/$m/$y"
        } else {
            ""
        }

        // 2) Invocar al caso de uso para validar
        val validation = UserValidator.validateProfile(
            fullName = candidate.fullName,
            dateOfBirthText = dateOfBirthText,
            selectedSexBackend = candidate.sex,
            phone = candidate.phone,
            dni = candidate.dni.toString()
        )

        if (validation is EditValidationResult.Error) {
            // 2.1) Si hay errores, los voltamos a UpdateState.ValidationErrors
            val fieldErrors = validation.fieldErrors.mapKeys { (campo, _) ->
                // Mapear ValidationResult.Field → UpdateState.Field
                when (campo) {
                    EditValidationResult.Field.FULL_NAME -> UpdateState.Field.FULL_NAME
                    EditValidationResult.Field.DATE_OF_BIRTH -> UpdateState.Field.DATE_OF_BIRTH
                    EditValidationResult.Field.SEX -> UpdateState.Field.SEX
                    EditValidationResult.Field.PHONE -> UpdateState.Field.PHONE
                    EditValidationResult.Field.DNI -> UpdateState.Field.DNI
                }
            }
            _updateState.value = UpdateState.ValidationErrors(fieldErrors)
            return
        }

        // 3) Si pasó validación, invocamos updateUser() en Loading
        _updateState.value = UpdateState.Loading

        viewModelScope.launch {
            val result = userUseCase.updateUser(candidate)

            result.onSuccess { newUser ->
                // Guardar en sesión y emitir Success
                SessionManager.storeUser(newUser)
                _updateState.value = UpdateState.Success(newUser)
            }.onFailure { throwable ->
                _updateState.value =
                    UpdateState.Error(throwable.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Opción para limpiar el estado luego de procesar el resultado en la UI.
     */
    fun clearUpdateState() {
        _updateState.value = UpdateState.Idle
    }

    fun deleteUser(email: String) {
        viewModelScope.launch {
            _deleteState.value = DeleteUserState.Loading

            userUseCase.deleteUser(email).fold(
                onSuccess = {
                    _deleteState.value = DeleteUserState.Success
                },
                onFailure = { throwable ->
                    _deleteState.value = when {
                        // si el mensaje de excepción incluye "no encontrado"
                        throwable.message?.contains("no encontrado", ignoreCase = true) == true ->
                            DeleteUserState.NotFound(email)
                        else ->
                            DeleteUserState.Error(throwable.message ?: "Error desconocido")
                    }
                }
            )
        }
    }

    /** Úsalo si quieres resetear el flujo (por ejemplo al salir de la pantalla) */
    fun resetDeleteState() {
        _deleteState.value = DeleteUserState.Idle
    }
}
