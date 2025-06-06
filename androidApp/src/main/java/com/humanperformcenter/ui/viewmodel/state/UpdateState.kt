package com.humanperformcenter.ui.viewmodel.state

import com.humanperformcenter.shared.data.model.LoginResponse

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    data class Success(val updatedUser: LoginResponse) : UpdateState()
    data class Error(val message: String) : UpdateState()

    /**
     * Errores de validación en campos concretos.
     * El key es el “nombre lógico” del campo, y el value es el mensaje a mostrar.
     */
    data class ValidationErrors(val fieldErrors: Map<Field, String>) : UpdateState()

    enum class Field {
        FULL_NAME,
        DATE_OF_BIRTH,
        SEX,
        PHONE,
        DNI
    }
}