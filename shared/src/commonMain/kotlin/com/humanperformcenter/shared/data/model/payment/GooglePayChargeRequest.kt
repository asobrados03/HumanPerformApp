package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.Serializable

@Serializable
data class GooglePayChargeRequest(
    val token: String,
    val amount: Int,
    val currency: String
)