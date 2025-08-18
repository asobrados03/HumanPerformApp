package com.humanperformcenter.shared.data.model

@kotlinx.serialization.Serializable
data class RebillRequest(
    val amount: Int,
    val currency: String,
    val user_id: Int
)
