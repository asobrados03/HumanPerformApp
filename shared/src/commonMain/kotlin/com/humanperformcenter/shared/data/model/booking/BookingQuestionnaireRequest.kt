package com.humanperformcenter.shared.data.model.booking

import kotlinx.serialization.Serializable

@Serializable
data class BookingQuestionnaireRequest(
    val booking_id: Int,
    val sleep_quality: String,
    val energy_level: String,
    val muscle_pain: String,
    val stress_level: String,
    val mood: String
)

