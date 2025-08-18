package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.data.model.RebillRequest

interface PaymentRepository {
    suspend fun generatePaymentUrl(request: PaymentRequest) : String
    suspend fun requestGooglePay(requestJson: String): String
    suspend fun sendTokenToBackend(token: String, amount: Int, currency: String): Boolean
    suspend fun getPaymentMethod(user_id: Int): String?
    suspend fun rebillPayment(rebillRequest: RebillRequest): Boolean
}
