package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ServicioItembien(
    val id: Int,
    val name: String,
    val description: String? = null,
    val price: Double? = null,
    val image: String? = null,
    val service_id: Int? = null,
    val isAvailable: Boolean? = true
)
