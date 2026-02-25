package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StripePaymentMethodsResponse(
    val success: Boolean,
    val data: List<StripePaymentMethod>
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
