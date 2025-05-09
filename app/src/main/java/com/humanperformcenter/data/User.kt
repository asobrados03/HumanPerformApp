package com.humanperformcenter.data

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
