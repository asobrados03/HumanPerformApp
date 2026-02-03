package com.humanperformcenter.shared.data.model.payment

data class CreateStripeCustomerResponse(
    val success: Boolean,
    val message: String? = null,
    val data: CustomerData? = null
)

data class CustomerData(
    val customerId: String,
    val isNew: Boolean
)