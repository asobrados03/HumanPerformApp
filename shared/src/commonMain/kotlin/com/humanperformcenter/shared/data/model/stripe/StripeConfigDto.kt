package com.humanperformcenter.shared.data.model.stripe

import kotlinx.serialization.Serializable

@Serializable data class StripeConfigDto(
    val publishableKey: String
)
