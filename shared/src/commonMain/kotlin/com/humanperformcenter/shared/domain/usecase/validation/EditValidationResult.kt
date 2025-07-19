package com.humanperformcenter.shared.domain.usecase.validation

/**
 * Resultado de la validación de campos de perfil.
 */
sealed class EditValidationResult {
    object Success : EditValidationResult()
    data class Error(val fieldErrors: Map<Field, String>) : EditValidationResult()

    enum class Field {
        FULL_NAME,
        DATE_OF_BIRTH,
        SEX,
        PHONE,
        POST_ADDRESS,
        DNI
    }
}