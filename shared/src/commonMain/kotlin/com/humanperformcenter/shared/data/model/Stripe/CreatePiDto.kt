package com.humanperformcenter.shared.data.model.Stripe

import kotlinx.serialization.Serializable

@Serializable
data class CreatePiDto(
    val clientSecret: String,
    val customerId: String? = null
)
