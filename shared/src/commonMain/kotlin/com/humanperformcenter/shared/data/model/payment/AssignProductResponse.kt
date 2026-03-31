package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class AssignProductResponse(
    val success: Boolean = true,
    @SerialName("assigned_id")
    @JsonNames("assignedId")
    val assignedId: Int? = null,
    val error: String? = null,
    val details: String? = null,
)
