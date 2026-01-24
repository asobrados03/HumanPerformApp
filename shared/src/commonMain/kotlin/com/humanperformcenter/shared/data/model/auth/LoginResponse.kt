package com.humanperformcenter.shared.data.model.auth

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
    val postAddress: String,
    val dni: String? = null,
    val profilePictureName: String?,
    val accessToken: String,
    val refreshToken: String
)
