package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReserveUpdateRequest(
    val booking_id: Int,
    val new_coach_id: Int,
    val new_service_id: Int,
    val new_product_id: Int,
    val new_session_timeslot_id: Int,
    val new_start_date: String // formato: "YYYY-MM-DD"
)
