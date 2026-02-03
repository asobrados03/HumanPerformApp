package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EphemeralKeyDto(
    val id: String,
    @SerialName("object") val objectType: String,
    @SerialName("associated_objects") val associatedObjects: List<AssociatedObject>,
    val created: Long,
    val expires: Long,
    val livemode: Boolean,
    val secret: String
)

@Serializable
data class AssociatedObject(
    val id: String,
    val type: String
)
