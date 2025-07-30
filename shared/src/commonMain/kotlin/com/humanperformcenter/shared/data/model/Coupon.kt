package com.humanperformcenter.shared.data.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Coupon(
    val id: Int,
    val code: String,
    val discount: Double,
    val isPercentage: Boolean,
    val expiryDate: LocalDate
)
