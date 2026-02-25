package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionDto(
    @SerialName("subscription_id")
    val subscriptionId: String,
    @SerialName("client_secret")
    val clientSecret: String,
    @SerialName("customer_id")
    val customerId: String
)
