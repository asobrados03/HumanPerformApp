package com.humanperformcenter.shared.data.model.product_service

import kotlinx.serialization.Serializable

@Serializable
data class SimpleService(
    val id: Int,
    val name: String
)