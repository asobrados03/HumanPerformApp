package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.PaymentMethod
import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.data.model.RebillRequest
import com.humanperformcenter.shared.domain.repository.PaymentRepository

class PaymentUseCase(private val paymentRepository: PaymentRepository) {
    suspend fun generatePaymentUrl(request: PaymentRequest): String {
        return paymentRepository.generatePaymentUrl(request)
    }

    suspend fun requestGooglePay(requestJson: String): String {
        return paymentRepository.requestGooglePay(requestJson)
    }

    suspend fun sendTokenToBackend(token: String, amount: Int, currency: String): Boolean {
        return paymentRepository.sendTokenToBackend(token, amount, currency)
    }

    suspend fun getPaymentMethod(userId: Int): String? {
        return paymentRepository.getPaymentMethod(userId)
    }

    suspend fun rebillPayment(rebillRequest: RebillRequest): Boolean {
        return paymentRepository.rebillPayment(rebillRequest)
    }

    suspend fun getPaymentMethods(userId: Int): List<PaymentMethod> {
        return paymentRepository.getPaymentMethods(userId)
    }
}