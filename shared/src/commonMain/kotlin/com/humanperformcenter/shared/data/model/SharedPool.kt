package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SharedPool(
    val services: List<Int>,
    val sessions: Int
)