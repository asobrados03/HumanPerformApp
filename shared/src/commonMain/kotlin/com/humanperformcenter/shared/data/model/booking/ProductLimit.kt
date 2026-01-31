package com.humanperformcenter.shared.data.model.booking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductLimit(
    @SerialName("product_id")
    val productId: Int,
    @SerialName("type_of_product")
    val typeOfProduct: String,
    @SerialName("weekly_limit")
    val weeklyLimit: Int? = null,
    @SerialName("total_limit")
    val totalLimit: Int? = null,
    val remaining: Int? = null
)
