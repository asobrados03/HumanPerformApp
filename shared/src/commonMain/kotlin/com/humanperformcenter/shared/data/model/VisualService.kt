package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VisualService(
    val id: Int,
    val name: String,
    val icon: String? = null
)
