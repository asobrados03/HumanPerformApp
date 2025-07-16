package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequest(
    val customer_id: Int,
    val product_id: Int,
    val email: String
)
