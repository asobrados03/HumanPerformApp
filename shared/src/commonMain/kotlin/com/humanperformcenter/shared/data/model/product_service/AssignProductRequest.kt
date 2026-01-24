package com.humanperformcenter.shared.data.model.product_service

import kotlinx.serialization.Serializable

@Serializable
data class AssignProductRequest(
    val user_id: Int,
    val product_id: Int,
    val payment_method: String,
    val coupon_code: String? = null,
)
