package com.humanperformcenter.shared.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(
    val email: String
)