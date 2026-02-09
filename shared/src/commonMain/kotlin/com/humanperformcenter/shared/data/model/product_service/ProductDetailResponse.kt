package com.humanperformcenter.shared.data.model.product_service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailResponse(
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("expiry_date")
    val expiryDate: String? = null,
    val amount: Double? = null,
    val discount: Double? = null,
    @SerialName("total_amount")
    val totalAmount: Double? = null,
    @SerialName("payment_method")
    val paymentMethod: String? = null,
    @SerialName("payment_status")
    val paymentStatus: String? = null,
    val name: String,
    val image: String? = null,
    val description: String? = null,
    val services: List<SimpleService>
)
