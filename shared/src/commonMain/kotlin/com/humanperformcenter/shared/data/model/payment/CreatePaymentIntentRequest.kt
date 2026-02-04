package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentIntentRequest(
    val amount: Double,
    val currency: String,
    val customerId: String,
    val paymentMethodId: String? = null,
    val metadata: Map<String, String>? = null
)
