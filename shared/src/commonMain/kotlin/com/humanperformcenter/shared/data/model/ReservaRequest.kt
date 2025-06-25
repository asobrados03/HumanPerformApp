package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReservaRequest (
    val customer_id: Int,
    val coach_id: Int,
    val session_timeslot_id: Int,
    val service_id: Int,
    val product_id: Int,
    val center_id: Int,
    val start_date: String, // formato ISO: "2025-06-24T10:00:00"
    val status: String = "active",
    val payment_status: String = "pending",
    val payment_method: String = "card"
)