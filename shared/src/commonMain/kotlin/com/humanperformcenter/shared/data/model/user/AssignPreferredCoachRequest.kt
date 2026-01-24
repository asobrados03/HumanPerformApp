package com.humanperformcenter.shared.data.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssignPreferredCoachRequest(
    @SerialName("service_name") val serviceName: String,
    @SerialName("customer_id")  val customerId: Int,
    @SerialName("coach_id")     val coachId: Int
)
