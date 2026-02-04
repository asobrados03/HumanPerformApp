package com.humanperformcenter.shared.presentation.ui

sealed class StartStripeCheckoutState {
    object Idle : StartStripeCheckoutState()
    object Loading : StartStripeCheckoutState()

    data class Ready(
        val clientSecret: String,
        val config: StripeCheckoutConfig
    ) : StartStripeCheckoutState()

    object Processing : StartStripeCheckoutState()

    object Completed : StartStripeCheckoutState()
    object Canceled : StartStripeCheckoutState()
    data class Failed(val message: String) : StartStripeCheckoutState()
}
