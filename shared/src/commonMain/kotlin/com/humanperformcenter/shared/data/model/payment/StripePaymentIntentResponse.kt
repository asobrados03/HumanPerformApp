package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.Serializable

@Serializable
data class StripePaymentIntentResponse(
    val success: Boolean,
    val data: CreatePiDto? = null // Aquí es donde realmente viven los campos
)
