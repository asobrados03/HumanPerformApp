package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.PaymentRequest

interface PaymentRepository {
    suspend fun generatePaymentUrl(request: PaymentRequest) : String
    suspend fun requestGooglePay(requestJson: String): String
    suspend fun sendTokenToBackend(token: String, amount: Int, currency: String): Boolean}