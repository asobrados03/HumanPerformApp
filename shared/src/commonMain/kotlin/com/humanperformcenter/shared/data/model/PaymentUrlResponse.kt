package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentUrlResponse(
    val paymentUrl: String
)
