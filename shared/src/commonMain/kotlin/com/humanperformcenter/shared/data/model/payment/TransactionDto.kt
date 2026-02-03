package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: String, // ID de base de datos o de stripe
    val amount: Double, // O Int si guardas en centavos
    val currency: String,
    val status: String,
    val description: String?,
    @SerialName("created_at") val createdAt: String? = null, // O Long si es timestamp
    @SerialName("payment_intent_id") val paymentIntentId: String? = null
)
