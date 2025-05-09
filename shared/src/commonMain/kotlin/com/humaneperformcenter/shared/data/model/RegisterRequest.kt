package com.humaneperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val nombre: String,
    val apellidos: String,
    val email: String,
    val telefono: String,
    val password: String,
    val sexo: String,
    val fechaNacimiento: String, // formato "YYYY-MM-DD"
    val codigoPostal: String,
    val dni: String
)
