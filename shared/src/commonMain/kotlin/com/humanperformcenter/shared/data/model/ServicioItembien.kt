package com.humanperformcenter.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServicioItembien(
    val id: Int,
    val name: String,
    val description: String? = null,
    val price: Double? = null,
    val image: String? = null,

    @SerialName("service_ids")
    val serviceIds: List<Int> = emptyList(),

    val isAvailable: Boolean? = true
)

