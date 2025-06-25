package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SesionesDia(
    val service_id: Int,
    val date: String,
    val hour: String,
    val coach_id: Int,
    val coach_name: String?,
    val booked: Int,
    val capacity: Int,
)
