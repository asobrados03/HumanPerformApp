package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailResponse(
    val created_at: String,
    val name: String,
    val image: String? = null,
    val description: String? = null,
    val valid_due: Int? = null,
    val services: List<SimpleService>
)
