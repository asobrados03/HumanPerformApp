package com.humanperformcenter.shared.domain.entities

data class BillingPrefill(
    val name: String? = null,
    val email: String? = null,
    val addressLine1: String? = null,
    val postalCode: String? = null,
    val city: String? = null
)
