package com.humanperformcenter.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("message") val message: String,
    @SerialName("token")   val token: String,
    @SerialName("user_id") val userId: Int
)
