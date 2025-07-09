package com.humanperformcenter.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SimpleService(
    val id: Int,
    val name: String
)