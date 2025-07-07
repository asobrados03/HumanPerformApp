package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserWeeklyLimitResponse(
    val weekly_limit: Map<Int, Int>,
    val unlimited_sessions: Map<Int, Int>
)
