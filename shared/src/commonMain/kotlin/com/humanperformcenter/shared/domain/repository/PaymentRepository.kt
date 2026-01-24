package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.payment.PaymentMethod
import com.humanperformcenter.shared.data.model.payment.PaymentRequest
import com.humanperformcenter.shared.data.model.payment.RebillRequest

interface PaymentRepository {
    suspend fun generatePaymentUrl(request: PaymentRequest) : String
    suspend fun requestGooglePay(requestJson: String): String
    suspend fun sendTokenToBackend(token: String, amount: Int, currency: String): Boolean
    suspend fun getPaymentMethod(userId: Int): String?
    suspend fun rebillPayment(rebillRequest: RebillRequest): Boolean
    suspend fun getPaymentMethods(userId: Int): List<PaymentMethod>
    fun getAllowedPaymentMethods(): String
    fun buildPaymentRequestJson(precio: Double): String
}
