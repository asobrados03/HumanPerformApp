package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String
)

