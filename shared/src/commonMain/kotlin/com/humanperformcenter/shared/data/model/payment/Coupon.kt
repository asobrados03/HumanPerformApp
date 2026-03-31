package com.humanperformcenter.shared.data.model.payment

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable

@Serializable
data class Coupon(
    val id: Int,
    val code: String,
    val discount: Double,
    val isPercentage: Boolean,
    val expiryDate: LocalDate,
    @JsonNames("product_ids", "productIds")
    val productIds: List<Int> = emptyList(),
)
