package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailResponse(
    val created_at: String,
    val expiry_date: String? = null,
    val amount: Double? = null,
    val discount: Double? = null,
    val total_amount: Double? = null,
    val payment_method: String? = null,
    val payment_status: String? = null,
    val name: String,
    val image: String? = null,
    val description: String? = null,
    val services: List<SimpleService>
)

