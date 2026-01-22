package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.domain.repository.PaymentRepository

class GooglePayUseCase(private val repository: PaymentRepository) {
    suspend operator fun invoke(paymentRequestJson: String, amount: Int, currency: String): Result<String> =
        try {
            val token = repository.requestGooglePay(paymentRequestJson)
            if (repository.sendTokenToBackend(token, amount, currency)) Result.success(token)
            else Result.failure(Exception("Backend no aprobó"))
        } catch (e: Exception) { Result.failure(e) }


    fun obtenerConfiguracionGPay(): String = repository.getAllowedPaymentMethods()

    fun prepararJsonPago(precio: Double): String = repository.buildPaymentRequestJson(precio)
}

