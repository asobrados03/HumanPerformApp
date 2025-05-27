package com.humanperformcenter.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val nombre: String,
    val apellidos: String,
    val email: String,
    val telefono: String,
    val password: String,
    val sexo: String,
    @SerialName("fecha_nacimiento")
    val fechaNacimiento: String, // formato "YYYY-MM-DD"
    @SerialName("codigo_postal")
    val codigoPostal: String,
    val dni: String
)
