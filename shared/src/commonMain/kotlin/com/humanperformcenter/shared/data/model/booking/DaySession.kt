package com.humanperformcenter.shared.data.model.booking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DaySession(
    @SerialName("product_id")
    val productId: Int,
    val date: String,
    val hour: String,
    @SerialName("coach_id")
    val coachId: Int,
    @SerialName("coach_name")
    val coachName: String? = null,
    val booked: Int,
    val capacity: Int,
)
