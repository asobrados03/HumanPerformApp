package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StripePaymentMethodsResponse(
    val success: Boolean,
    val data: StripePaymentMethodsContainer // El "objeto" intermedio
)

@Serializable
data class StripePaymentMethodsContainer(
    val methods: List<StripePaymentMethod>, // La lista de tarjetas
    val defaultPaymentMethodId: String? = null // El ID que extraes del cliente
)

@Serializable
data class StripePaymentMethod(
    val id: String,
    val customer: String,
    val card: StripeCardDetails,
    val type: String
)

@Serializable
data class StripeCardDetails(
    val brand: String,
    val last4: String,
    @SerialName("exp_month") val expMonth: Int,
    @SerialName("exp_year") val expYear: Int,
    @SerialName("display_brand") val displayBrand: String? = null
)
