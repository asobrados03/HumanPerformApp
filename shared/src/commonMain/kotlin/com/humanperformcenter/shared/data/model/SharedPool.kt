package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SharedPool(
    val services: List<Int>,
    val sessions: Int,
    val valid_from: String? = null, // ISO-8601 o null
    val valid_to: String? = null    // ISO-8601 o null
)