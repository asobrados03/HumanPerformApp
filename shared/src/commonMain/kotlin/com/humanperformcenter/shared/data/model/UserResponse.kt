package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Int,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val telefono: String? = null,
    val sexo: String? = null,
    val fechaNacimiento: String? = null,
    val codigoPostal: String? = null,
    val dni: String? = null,
    // El token JWT o UUID que devuelva la API
    val token: String? = null
)
