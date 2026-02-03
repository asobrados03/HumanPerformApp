package com.humanperformcenter.shared.presentation.ui

data class StripeCheckoutConfig(
    val merchantDisplayName: String,
    val allowsDelayedPaymentMethods: Boolean = true,
    val customerId: String? = null,
    val customerEphemeralKeySecret: String? = null,
    val googlePayEnabled: Boolean = false,
    val googlePayCountryCode: String? = null,
    val googlePayCurrencyCode: String? = null,
    val billingName: String? = null,
    val billingEmail: String? = null,
    val billingAddressLine1: String? = null,
    val billingPostalCode: String? = null,
    val billingCity: String? = null
)
