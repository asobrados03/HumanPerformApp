package com.humanperformcenter.shared.data.model.Stripe

import kotlinx.serialization.Serializable

@Serializable data class EphemeralKeyDto(
    val id: String,
    val secret: String
)
