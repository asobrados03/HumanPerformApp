package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.Serializable

@Serializable
data class PublishableKeyResponse(
    val publishableKey: String
)
