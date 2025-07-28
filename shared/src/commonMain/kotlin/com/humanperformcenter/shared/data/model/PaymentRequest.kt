package com.humanperformcenter.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequest(
    @SerialName("customer_id")
    val customerId: Int,
    @SerialName("product_id")
    val productId: Int,
    val email: String,
    @SerialName("billing_street")
    val billingStreet: String,
    @SerialName("billing_postal")
    val billingPostal: String
)