package com.humanperformcenter.shared.data.model.Stripe

import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentIntentRequest(
    val amount: Int,                 // en céntimos
    val currency: String,            // "EUR"
    val user_id: Int? = null,
    val product_id: Int? = null,
    val metadata: Map<String, String> = emptyMap() // ¡solo String->String!
)
