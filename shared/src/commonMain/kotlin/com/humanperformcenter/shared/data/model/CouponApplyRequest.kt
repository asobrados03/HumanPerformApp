package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CouponApplyRequest(
    val coupon_code: String,
    val user_id: Int,
    val product_id: Int
)
