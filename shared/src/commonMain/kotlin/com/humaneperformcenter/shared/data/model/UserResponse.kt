package com.humaneperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Int,
    val nombre: String,
    val apellidos: String,
    val email: String,
    // otros campos que devuelva el servidor al registrar, por ejemplo token JWT:
    val token: String? = null
)