package com.humanperformcenter.shared.domain.usecase.validation

sealed class RegisterValidationResult {
    object Success : RegisterValidationResult()
    data class Error(val fieldErrors: Map<RegisterField, String>) : RegisterValidationResult()

    /**
     * Campos de validación para el registro.
     */
    enum class RegisterField {
        FIRST_NAME,
        LAST_NAME,
        EMAIL,
        PHONE,
        PASSWORD,
        DATE_OF_BIRTH,
        SEX,
        POSTCODE,
        POSTAL_ADDRESS,
        DNI
    }
}