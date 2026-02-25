package com.humanperformcenter.shared.data.model.product_service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Int,
    val name: String,
    val description: String? = null,
    val price: Double? = null,
    val image: String? = null,
    @SerialName("type_of_product")
    val typeOfProduct: String? = null,
    @SerialName("price_id")
    val priceId: String? = null,
    val session: Int? = null,

    @SerialName("service_ids")
    val serviceIds: List<Int> = emptyList(),

    val isAvailable: Boolean? = true,

    // --- NUEVOS CAMPOS DE STRIPE ---
    @SerialName("stripe_subscription_id")
    val stripeSubscriptionId: String? = null,

    @SerialName("stripe_payment_intent_id")
    val stripePaymentIntentId: String? = null
)
