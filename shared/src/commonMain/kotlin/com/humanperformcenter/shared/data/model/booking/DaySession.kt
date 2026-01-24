package com.humanperformcenter.shared.data.model.booking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DaySession(
    @SerialName("service_id")
    val serviceId: Int,
    val date: String,
    val hour: String,
    @SerialName("coach_id")
    val coachId: Int,
    @SerialName("coach_name")
    val coachName: String?,
    val booked: Int,
    val capacity: Int,
)
