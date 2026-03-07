package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.Serializable

@Serializable
data class CreateRefundRequest(
    val paymentIntentId: String,
    val amount: Double? = null
)
