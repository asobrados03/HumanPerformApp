package com.humanperformcenter.shared.data.model.product_service

import kotlinx.serialization.Serializable

@Serializable
data class ServiceAvailable(
    val id: Int,
    val name: String,
    val image: String? = null
)
