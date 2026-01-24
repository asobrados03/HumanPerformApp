package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.Serializable

@Serializable
data class RebillRequest(
    val amount: Int,
    val currency: String,
    val user_id: Int,
    val product_id: Int
)
