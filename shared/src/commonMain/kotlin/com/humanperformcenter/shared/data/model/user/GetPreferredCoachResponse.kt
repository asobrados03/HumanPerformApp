package com.humanperformcenter.shared.data.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPreferredCoachResponse(
    @SerialName("preferred_coach_id")
    val coachId: Int
)