package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateStripeCustomerResponse(
    val success: Boolean,
    val message: String? = null,
    val data: CustomerData? = null
)

@Serializable
data class CustomerData(
    @SerialName("customerId")
    val customerId: String,

    @SerialName("isNew")
    val isNew: Boolean
)