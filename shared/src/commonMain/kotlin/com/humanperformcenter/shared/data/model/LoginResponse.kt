package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val id: Int,
    val fullName: String,
    val email: String,
    val phone: String,
    val sex: String,
    val dateOfBirth: String,
    val postcode: Int? = null,
    val dni: String? = null,
    val profilePictureUrl: String?,
    val accessToken: String,
    val refreshToken: String
)
