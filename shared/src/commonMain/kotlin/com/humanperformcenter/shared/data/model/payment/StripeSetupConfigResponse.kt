package com.humanperformcenter.shared.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StripeSetupConfigResponse(
    val success: Boolean,
    val data: StripeSetupConfigData? = null
)

@Serializable
data class StripeSetupConfigData(
    val customerId: String? = null,
    @SerialName("customer_id") val customerIdSnake: String? = null,
    val clientSecret: String? = null,
    @SerialName("client_secret") val clientSecretSnake: String? = null,
    val ephemeralKey: String? = null,
    @SerialName("ephemeral_key") val ephemeralKeySnake: String? = null,
    val publishableKey: String? = null,
    @SerialName("publishable_key") val publishableKeySnake: String? = null
) {
    val resolvedCustomerId: String?
        get() = customerId ?: customerIdSnake

    val resolvedClientSecret: String?
        get() = clientSecret ?: clientSecretSnake

    val resolvedEphemeralKey: String?
        get() = ephemeralKey ?: ephemeralKeySnake

    val resolvedPublishableKey: String?
        get() = publishableKey ?: publishableKeySnake
}
