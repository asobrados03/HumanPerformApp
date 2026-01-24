package com.humanperformcenter.shared.data.model.booking

import kotlinx.serialization.Serializable

@Serializable
data class ReserveResponse(
    val message: String,
    val booking_id: Int
)