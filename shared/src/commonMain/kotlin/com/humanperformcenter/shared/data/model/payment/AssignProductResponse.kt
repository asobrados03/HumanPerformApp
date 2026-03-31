package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssignProductResponse(
    val success: Boolean = true,
    @SerialName("assigned_id")
    val assignedId: Int? = null,
    val error: String? = null,
    val details: String? = null,
)
