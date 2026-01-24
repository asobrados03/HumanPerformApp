package com.humanperformcenter.shared.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val userId: Int
)
