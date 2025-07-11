package com.humanperformcenter.shared.data.model

data class PaymentRequest(
    val amount: String,
    val currency: String,
    val country: String,
    val customerId: String,
    val customerEmail: String,
    val transactionId: String,
    val successUrl: String,
    val errorUrl: String,
    val cancelUrl: String,
    val statusUrl: String
)
