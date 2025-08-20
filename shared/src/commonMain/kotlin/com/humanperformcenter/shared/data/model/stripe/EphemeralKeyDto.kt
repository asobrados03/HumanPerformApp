package com.humanperformcenter.shared.data.model.stripe

import kotlinx.serialization.Serializable

@Serializable data class EphemeralKeyDto(
    val id: String,
    val secret: String
)
