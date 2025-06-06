package com.humanperformcenter.ui.viewmodel.state

import com.humanperformcenter.shared.data.model.RegisterResponse
import com.humanperformcenter.shared.domain.usecase.validation.RegisterValidationResult.RegisterField

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: RegisterResponse) : RegisterState()
    data class Error(val message: String) : RegisterState()

    /**
     * Errores de validación en campos de registro.
     * El mapa asocia cada campo (RegisterField) a su mensaje de error.
     */
    data class ValidationErrors(val fieldErrors: Map<RegisterField, String>) : RegisterState()
}
