package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionDto(
    val id: String,
    @SerialName("object") val objectType: String = "subscription",
    val status: String,
    @SerialName("current_period_start") val currentPeriodStart: Long,
    @SerialName("current_period_end") val currentPeriodEnd: Long,
    val customer: String,
    @SerialName("default_payment_method") val defaultPaymentMethod: String? = null,
    // Dependiendo de si Stripe expande los items o no, esto podría variar,
    // pero generalmente es una lista de items
    val items: SubscriptionItems? = null
)

@Serializable
data class SubscriptionItems(
    @SerialName("object") val objectType: String = "list",
    val data: List<SubscriptionItem>
)

@Serializable
data class SubscriptionItem(
    val id: String,
    val price: PriceDto
)

@Serializable
data class PriceDto(
    val id: String,
    @SerialName("unit_amount") val unitAmount: Int?,
    val currency: String
)
