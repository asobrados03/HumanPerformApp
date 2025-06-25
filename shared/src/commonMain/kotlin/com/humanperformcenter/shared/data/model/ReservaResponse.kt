package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReservaResponse(
    val message: String,
    val booking_id: Int
)