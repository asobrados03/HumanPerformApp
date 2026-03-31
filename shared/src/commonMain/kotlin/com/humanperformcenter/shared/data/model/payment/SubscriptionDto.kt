package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class SubscriptionDto(
    @SerialName("subscription_id")
    @JsonNames("id")
    val subscriptionId: String = "",
    @SerialName("client_secret")
    val clientSecret: String? = null,
    @SerialName("customer_id")
    val customerId: String = "",
)
