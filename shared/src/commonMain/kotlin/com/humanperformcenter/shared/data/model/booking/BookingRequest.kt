package com.humanperformcenter.shared.data.model.booking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookingRequest (
    @SerialName("customer_id")
    val customerId: Int,
    @SerialName("coach_id")
    val coachId: Int,
    @SerialName("session_timeslot_id")
    val sessionTimeslotId: Int,
    @SerialName("service_id")
    val serviceId: Int,
    @SerialName("product_id")
    val productId: Int,
    @SerialName("center_id")
    val centerId: Int,
    @SerialName("start_date")
    val startDate: String, // formato ISO: "2025-06-24T10:00:00"
    val status: String = "active",
    @SerialName("payment_status")
    val paymentStatus: String = "pending",
    @SerialName("payment_method")
    val paymentMethod: String = "card"
)