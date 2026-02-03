package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethodDto(
    val id: String,
    @SerialName("object") val objectType: String = "payment_method",
    val type: String,
    @SerialName("billing_details") val billingDetails: BillingDetails?,
    val card: CardDetails? = null,
    val created: Long? = null,
    val customer: String? = null
)

@Serializable
data class BillingDetails(
    val email: String?,
    val name: String?,
    val phone: String?
)

@Serializable
data class CardDetails(
    val brand: String,
    val checks: CardChecks?,
    val country: String?,
    @SerialName("exp_month") val expMonth: Int,
    @SerialName("exp_year") val expYear: Int,
    val funding: String?,
    @SerialName("last4") val last4: String
)

@Serializable
data class CardChecks(
    @SerialName("cvc_check") val cvcCheck: String?,
    @SerialName("address_line1_check") val addressLine1Check: String?,
    @SerialName("address_postal_code_check") val addressPostalCodeCheck: String?
)