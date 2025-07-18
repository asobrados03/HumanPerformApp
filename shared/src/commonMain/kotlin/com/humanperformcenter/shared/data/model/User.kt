package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val fullName: String,
    val email: String,
    val phone: String,
    val sex: String,
    val dateOfBirth: String,
    val postcode: Int? = null,
    val postAddress: String? = null,
    val dni: String? = null,
    val profilePictureName: String?
)
