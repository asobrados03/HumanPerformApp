package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.Serializable

@Serializable
data class StripeEphemeralKeyResponse(
    val success: Boolean,
    val data: EphemeralKeyDto? = null // El objeto real está aquí dentro
)
