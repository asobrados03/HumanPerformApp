package com.humanperformcenter.shared.data.model.booking

import kotlinx.serialization.Serializable

@Serializable
data class WeeklyLimitsWrapper(
    val weekly_limits: List<ProductLimit>
)
