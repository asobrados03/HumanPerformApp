package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val dateOfBirth: String,
    val gender: String,
    val profilePictureUrl: String?,
    val balance: Double
)