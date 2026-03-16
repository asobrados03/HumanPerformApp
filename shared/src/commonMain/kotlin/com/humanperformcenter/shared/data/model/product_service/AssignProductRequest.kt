package com.humanperformcenter.shared.data.model.product_service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssignProductRequest(
    @SerialName("product_id") val productId: Int,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("coupon_code") val couponCode: String?,
    // El backend acepta 'centro' opcionalmente, añádelo si lo usas
    //val centro: String? = null
)
