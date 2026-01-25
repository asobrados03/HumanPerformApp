package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.auth.RegisterResponse
import com.humanperformcenter.shared.domain.usecase.validation.RegisterValidationResult.RegisterField

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val message: RegisterResponse) : RegisterState()
    data class Error(val message: String) : RegisterState()

    /**
     * Errores de validación en campos de registro.
     * El mapa asocia cada campo (RegisterField) a su mensaje de error.
     */
    data class ValidationErrors(val fieldErrors: Map<RegisterField, String>) : RegisterState()
}
