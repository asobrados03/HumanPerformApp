package com.humaneperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val message: String,
    val status: String
)
