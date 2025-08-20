package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.PaymentMethod
import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.data.model.RebillRequest
import com.humanperformcenter.shared.domain.repository.PaymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class PaymentUseCase(private val paymentRepository: PaymentRepository) {
    suspend fun generatePaymentUrl(request: PaymentRequest): String = withContext(Dispatchers.IO) {
        return@withContext paymentRepository.generatePaymentUrl(request)
    }
    suspend fun requestGooglePay(requestJson: String): String = withContext(Dispatchers.IO) {
        return@withContext paymentRepository.requestGooglePay(requestJson)
    }
    suspend fun sendTokenToBackend(token: String, amount: Int, currency: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext paymentRepository.sendTokenToBackend(token, amount, currency)
    }
    suspend fun getPaymentMethod(userId: Int): String? = withContext(Dispatchers.IO) {
        return@withContext paymentRepository.getPaymentMethod(userId)
    }
    suspend fun rebillPayment(rebillRequest: RebillRequest): Boolean = withContext(Dispatchers.IO) {
        return@withContext paymentRepository.rebillPayment(rebillRequest)
    }
    suspend fun getPaymentMethods(userId: Int): List<PaymentMethod> = withContext(Dispatchers.IO) {
        return@withContext paymentRepository.getPaymentMethods(userId)
    }
}