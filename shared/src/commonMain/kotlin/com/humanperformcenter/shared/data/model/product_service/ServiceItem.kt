package com.humanperformcenter.shared.data.model.product_service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceItem(
    val id: Int,
    val name: String,
    val description: String? = null,
    val price: Double? = null,
    val image: String? = null,
    @SerialName("type_of_product")
    val tipo_producto: String? = null,  // "recurrent", "multi_sessions", etc.
    val session: Int? = null,

    @SerialName("service_ids")
    val serviceIds: List<Int> = emptyList(),

    val isAvailable: Boolean? = true
)
