package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.domain.repository.UserRepository

/**
 * Resultado de la validación de campos de perfil.
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val fieldErrors: Map<Field, String>) : ValidationResult()

    enum class Field {
        FULL_NAME,
        DATE_OF_BIRTH,
        SEX,
        PHONE,
        DNI
    }
}

class UserUseCase(private val userRepository: UserRepository) {
    /**
    * Valida en el dominio cada campo obligatorio del perfil:
    * - fullName no vacío
    * - dateOfBirthText debe estar en formato "dd/MM/yyyy" y rangos válidos
    * - selectedSexBackend debe ser "Male" o "Female"
    * - phone al menos 9 dígitos numéricos
    *
    * Retorna Success si todo está correcto, o Error con un mapa de (campo→mensaje).
    */
    fun validateProfile(
        fullName: String,
        dateOfBirthText: String,
        selectedSexBackend: String?,
        phone: String,
        dni: String
    ): ValidationResult {
        val errors = mutableMapOf<ValidationResult.Field, String>()

        // 1) Validar nombre
        if (fullName.isBlank()) {
            errors[ValidationResult.Field.FULL_NAME] = "El nombre es obligatorio"
        }

        // 2) Validar fecha: "dd/MM/yyyy"
        if (dateOfBirthText.isBlank()) {
            errors[ValidationResult.Field.DATE_OF_BIRTH] = "La fecha de nacimiento es obligatoria"
        } else {
            val partes = dateOfBirthText.split("/")
            if (partes.size != 3 ||
                partes.any { it.isBlank() } ||
                partes.any { parte -> !parte.all { ch -> ch.isDigit() } }
            ) {
                errors[ValidationResult.Field.DATE_OF_BIRTH] = "Fecha inválida. Usa dd/MM/yyyy"
            } else {
                val d = partes[0].toIntOrNull() ?: -1
                val m = partes[1].toIntOrNull() ?: -1
                val y = partes[2].toIntOrNull() ?: -1
                if (d !in 1..31 || m !in 1..12 || y < 1900) {
                    errors[ValidationResult.Field.DATE_OF_BIRTH] = "Fecha fuera de rango"
                }
            }
        }

        // 3) Validar sexo
        if (selectedSexBackend.isNullOrBlank() ||
            (selectedSexBackend != "Male" && selectedSexBackend != "Female")
        ) {
            errors[ValidationResult.Field.SEX] = "Debes seleccionar un sexo"
        }

        // 4) Validar teléfono
        if (phone.isBlank()) {
            errors[ValidationResult.Field.PHONE] = "El teléfono es obligatorio"
        } else if (phone.any { !it.isDigit() } || phone.length < 9) {
            errors[ValidationResult.Field.PHONE] = "Teléfono inválido"
        }

        // 5) Validar DNI (si se ha ingresado algo)
        //    El formato español: 7 u 8 dígitos seguidos de una letra. La letra debe coincidir con el checksum.
        if (dni.isNotBlank()) {
            if (!isValidSpanishDNI(dni.uppercase())) {
                errors[ValidationResult.Field.DNI] = "DNI inválido"
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }

    /**
     * Comprueba si la cadena cumple el formato DNI español (“NNNNNNNNL” o “NNNNNNNL”),
     * y si la letra final coincide con el checksum oficial.
     */
    private fun isValidSpanishDNI(dni: String): Boolean {
        // Regex: 7 u 8 dígitos, seguidos de 1 letra (A–Z)
        val regex = Regex("""^(\d{7,8})([A-Z])${'$'}""")
        val match = regex.matchEntire(dni) ?: return false

        val numberPart = match.groupValues[1]   // "12345678"
        val letterPart = match.groupValues[2][0]// 'Z'

        // Tabla de letras para DNI: índice = (número mod 23)
        val tabla = "TRWAGMYFPDXBNJZSQVHLCKE"
        val num = numberPart.toLongOrNull() ?: return false
        val idx = (num % 23).toInt()
        val expectedLetter = tabla[idx]

        return letterPart == expectedLetter
    }

    /**
     * Si la validación pasó, envía la actualización al repositorio.
     * La llamada al repositorio puede devolver Result.success(nuevoUser) o Result.failure(t).
     */
    suspend fun updateUser(user: LoginResponse): Result<LoginResponse> {
        return userRepository.updateUser(user)
    }
}