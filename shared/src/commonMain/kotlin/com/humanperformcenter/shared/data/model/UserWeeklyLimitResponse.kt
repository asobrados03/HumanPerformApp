package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserWeeklyLimitResponse(
    val weekly_limit: Map<Int, Int> = emptyMap(),
    val unlimited_sessions: Map<Int, Int> = emptyMap(),
    val unlimited_shared: List<SharedPool> = emptyList(),
    val service_to_primary: Map<Int, Int> = emptyMap()
)

