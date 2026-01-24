package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CouponApplyRequest(
    @SerialName("coupon_code")
    val couponCode: String,
    @SerialName("user_id")
    val userId: Int,
    @SerialName("product_id")
    val productId: Int
)
