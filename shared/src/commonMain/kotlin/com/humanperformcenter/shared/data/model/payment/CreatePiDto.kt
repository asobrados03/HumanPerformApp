package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePiDto(
    val id: String,
    @SerialName("object") val objectType: String = "payment_intent",
    val amount: Int,
    val currency: String,
    @SerialName("client_secret") val clientSecret: String,
    val status: String,
    @SerialName("payment_method") val paymentMethodId: String? = null,
    val created: Long
)
