package com.humanperformcenter.shared.data.model.Stripe

import kotlinx.serialization.Serializable

@Serializable data class StripeConfigDto(
    val publishableKey: String
)
