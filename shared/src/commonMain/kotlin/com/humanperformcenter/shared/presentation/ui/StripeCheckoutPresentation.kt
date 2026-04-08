package com.humanperformcenter.shared.presentation.ui

data class StripeCheckoutPresentation(
    val id: Long,
    val clientSecret: String,
    val config: StripeCheckoutConfig
)
