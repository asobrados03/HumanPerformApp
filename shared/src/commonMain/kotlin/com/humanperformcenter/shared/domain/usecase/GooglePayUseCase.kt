package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.domain.repository.PaymentRepository

class GooglePayUseCase(private val repo: PaymentRepository) {
    suspend operator fun invoke(
        paymentRequestJson: String,
        amountInCents: Int,
        currency: String
    ): Result<String> = try {
        val token = repo.requestGooglePay(paymentRequestJson)
        if (repo.sendTokenToBackend(token, amountInCents, currency)) Result.success(token)
        else Result.failure(Exception("Backend no aprobó"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

