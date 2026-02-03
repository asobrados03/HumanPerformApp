package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CardDto(
    val id: String, // ID interno de tu BD (cardId)
    @SerialName("user_id") val userId: String,
    @SerialName("payment_method_id") val paymentMethodId: String,
    val brand: String,
    val last4: String,
    @SerialName("exp_month") val expMonth: Int,
    @SerialName("exp_year") val expYear: Int,
    @SerialName("is_default") val isDefault: Boolean = false
)
