package com.humanperformcenter.shared.domain.usecase.validation

import com.humanperformcenter.shared.domain.usecase.validation.RegisterValidationResult.RegisterField
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object UserValidator {
    /**
     * Valida en el dominio cada campo obligatorio del perfil:
     * - fullName no vacío
     * - dateOfBirthText debe estar en formato "DD/MM/YYYY" y rangos válidos
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
        postAddress: String,
        dni: String
    ): EditValidationResult {
        val errors = mutableMapOf<EditValidationResult.Field, String>()

        // 1) Validar nombre
        if (fullName.isBlank()) {
            errors[EditValidationResult.Field.FULL_NAME] = "El nombre es obligatorio"
        }

        // 2) Validar fecha: "dd/MM/yyyy"
        if (dateOfBirthText.isBlank()) {
            errors[EditValidationResult.Field.DATE_OF_BIRTH] = "La fecha de nacimiento es obligatoria"
        } else {
            val partes = dateOfBirthText.split("/")
            if (partes.size != 3 ||
                partes.any { it.isBlank() } ||
                partes.any { parte -> !parte.all { ch -> ch.isDigit() } }
            ) {
                errors[EditValidationResult.Field.DATE_OF_BIRTH] = "Fecha inválida. Usa DD/MM/YYYY"
            } else {
                val d = partes[0].toIntOrNull() ?: -1
                val m = partes[1].toIntOrNull() ?: -1
                val y = partes[2].toIntOrNull() ?: -1
                if (d !in 1..31 || m !in 1..12 || y < 1900) {
                    errors[EditValidationResult.Field.DATE_OF_BIRTH] = "Fecha fuera de rango"
                }
            }
        }

        // 3) Validar sexo
        if (selectedSexBackend.isNullOrBlank() ||
            (selectedSexBackend != "Male" && selectedSexBackend != "Female")
        ) {
            errors[EditValidationResult.Field.SEX] = "Debes seleccionar un sexo"
        }

        // 4) Validar teléfono
        if (phone.isBlank()) {
            errors[EditValidationResult.Field.PHONE] = "El teléfono es obligatorio"
        } else if (phone.any { !it.isDigit() } || phone.length < 9) {
            errors[EditValidationResult.Field.PHONE] = "Teléfono inválido"
        }

        if (postAddress.isBlank()) {
            errors[EditValidationResult.Field.POST_ADDRESS] = "La dirección postal es obligatoria"
        }

        // 5) Validar DNI (si se ha ingresado algo)
        if (!dni.endsWith(".")){
            if (dni.isNotBlank()) {
                if (!isValidSpanishDNIAndNIE(dni.uppercase())) {
                    errors[EditValidationResult.Field.DNI] = "DNI o NIE inválido"
                }
            }
        }

        return if (errors.isEmpty()) {
            EditValidationResult.Success
        } else {
            EditValidationResult.Error(errors)
        }
    }

    /**
     * Valida todos los campos de registro:
     * - firstName / lastName: no vacíos
     * - email: no vacío + formato básico con '@'
     * - phone: no vacío + mínimo 9 dígitos numéricos
     * - password: no vacío + al menos 6 caracteres (por ejemplo)
     * - dateOfBirthText: "dd/MM/yyyy" + rangos válidos
     * - selectedSexBackend: debe ser "Male" o "Female"
     * - postcode: no vacío + solo dígitos + mínimo 5 cifras
     * - dni: si no está vacío, debe ser válido (8 dígitos + letra checksum)
     * - términos y política: ambos true
     */
    @OptIn(ExperimentalTime::class)
    fun validateRegister(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
        dateOfBirthText: String,
        selectedSexBackend: String?,
        postcode: String,
        address: String,
        dni: String
    ): RegisterValidationResult {
        val errors = mutableMapOf<RegisterField, String>()

        // 1) Nombre y apellidos
        if (firstName.isBlank()) {
            errors[RegisterField.FIRST_NAME] = "El nombre es obligatorio"
        }
        if (lastName.isBlank()) {
            errors[RegisterField.LAST_NAME] = "Los apellidos son obligatorios"
        }

        // 2) Email
        if (email.isBlank()) {
            errors[RegisterField.EMAIL] = "El correo electrónico es obligatorio"
        } else {
            val regexEmail = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
            if (!regexEmail.matches(email.trim())) {
                errors[RegisterField.EMAIL] = "Correo electrónico inválido"
            }
        }

        // 3) Teléfono
        if (phone.isBlank()) {
            errors[RegisterField.PHONE] = "El teléfono es obligatorio"
        } else if (phone.any { !it.isDigit() } || phone.length < 9) {
            errors[RegisterField.PHONE] = "Teléfono inválido"
        }

        // 4) Contraseña
        if (password.isBlank()) {
            errors[RegisterField.PASSWORD] = "La contraseña es obligatoria"
        } else if (password.length < 8) {
            errors[RegisterField.PASSWORD] = "La contraseña debe tener al menos 8 caracteres"
        } else if (!password.contains(".*\\d.*".toRegex())) {
            errors[RegisterField.PASSWORD] = "La contraseña debe contener al menos un número"
        } else if (!password.contains("[A-Z]".toRegex())) {
            errors[RegisterField.PASSWORD] = "La nueva contraseña debe contener al menos una mayúscula"
        } else if(!password.contains("[a-z]".toRegex())) {
            errors[RegisterField.PASSWORD] = "La nueva contraseña debe contener al menos una minúscula"
        }

        // 5) Fecha de nacimiento
        if (dateOfBirthText.isBlank()) {
            errors[RegisterField.DATE_OF_BIRTH] = "La fecha de nacimiento es obligatoria"
        } else {
            val date: LocalDate? = try {
                // Verificar que sea formato ddMMyyyy (exactamente 8 dígitos)
                if (dateOfBirthText.length != 8 || !dateOfBirthText.all { it.isDigit() }) {
                    throw IllegalArgumentException()
                }

                val day = dateOfBirthText.substring(0, 2).toInt()
                val month = dateOfBirthText.substring(2, 4).toInt()
                val year = dateOfBirthText.substring(4, 8).toInt()

                // Validaciones básicas de rangos
                if (day !in 1..31 || month !in 1..12 || year < 1900) {
                    throw IllegalArgumentException()
                }

                // Usar parse con formato ISO estándar (yyyy-MM-dd)
                val isoDateString = "${year.toString().padStart(4, '0')}-${month.toString()
                    .padStart(2, '0')}-${day.toString().padStart(2, '0')}"

                LocalDate.parse(isoDateString)

            } catch (_: Exception) {
                null
            }

            when {
                date == null -> {
                    errors[RegisterField.DATE_OF_BIRTH] = "Fecha inválida"
                }
                date.year < 1900 -> {
                    errors[RegisterField.DATE_OF_BIRTH] = "Fecha demasiado antigua"
                }
                else -> {
                    // Compara contra hoy en la zona del dispositivo
                    val today = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                    if (date > today) {
                        errors[RegisterField.DATE_OF_BIRTH] = "La fecha no puede ser futura"
                    }
                }
            }
        }

        // 6) Sexo
        if (selectedSexBackend.isNullOrBlank() ||
            (selectedSexBackend != "Male" && selectedSexBackend != "Female")
        ) {
            errors[RegisterField.SEX] = "Debes seleccionar un sexo"
        }

        // 7) Código Postal
        if (postcode.isBlank()) {
            errors[RegisterField.POSTCODE] = "El código postal es obligatorio"
        } else if (postcode.any { !it.isDigit() } || postcode.length < 5) {
            errors[RegisterField.POSTCODE] = "Código postal inválido"
        }

        // Dirección postal
        if (address.isBlank()) {
            errors[RegisterField.POSTAL_ADDRESS] = "La dirección postal es obligatoria"
        }

        // 8) DNI
        // Permitir DNIs que terminan en punto para pruebas internas
        if (!dni.endsWith(".")){
            if (dni.isBlank()) {
                errors[RegisterField.DNI] = "El DNI es obligatorio"
            } else if (!isValidSpanishDNIAndNIE(dni.uppercase())) {
                errors[RegisterField.DNI] = "DNI inválido"
            }
        }

        return if (errors.isEmpty()) {
            RegisterValidationResult.Success
        } else {
            RegisterValidationResult.Error(errors)
        }
    }

    /**
     * Comprueba si la cadena cumple el formato DNI español (“NNNNNNNNL” o “NNNNNNNL”),
     * y si la letra final coincide con el checksum oficial.
     */
    private fun isValidSpanishDNIAndNIE(input: String): Boolean {
        val upper = input.uppercase()

        // Extrae letra final
        val letter = upper.lastOrNull() ?: return false
        val body = upper.dropLast(1)

        // Reemplaza letra inicial del NIE por número correspondiente
        val numericPart = when {
            body.matches(Regex("\\d{7,8}")) -> body // DNI
            body.matches(Regex("X\\d{7}")) -> body.replaceFirst("X", "0")
            body.matches(Regex("Y\\d{7}")) -> body.replaceFirst("Y", "1")
            body.matches(Regex("Z\\d{7}")) -> body.replaceFirst("Z", "2")
            else -> return false
        }

        val num = numericPart.toLongOrNull() ?: return false
        val expectedLetter = "TRWAGMYFPDXBNJZSQVHLCKE"[ (num % 23).toInt() ]

        return letter == expectedLetter
    }
}