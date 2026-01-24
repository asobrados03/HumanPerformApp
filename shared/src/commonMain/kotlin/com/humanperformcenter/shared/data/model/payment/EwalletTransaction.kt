package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.Serializable

@Serializable
data class EwalletTransaction(
    val amount: Double,
    val balance: Double,
    val description: String,
    val type: String,
    val date: String
)
