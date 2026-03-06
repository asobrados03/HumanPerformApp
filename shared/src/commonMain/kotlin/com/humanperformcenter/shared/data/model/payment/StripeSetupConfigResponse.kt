package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StripeSetupConfigResponse(
    val success: Boolean,
    val message: String? = null,
    val data: StripeSetupConfigData? = null
)

@Serializable
data class StripeSetupConfigData(
    @SerialName("customer_id")
    val customerId: String? = null,
    @SerialName("setup_intent_client_secret")
    val clientSecret: String? = null,
    @SerialName("ephemeral_key")
    val ephemeralKey: String? = null,
    @SerialName("setup_intent_id")
    val setupIntentId: String? = null,
    //@SerialName("publishable_key")
    //val publishableKey: String? = null
)
