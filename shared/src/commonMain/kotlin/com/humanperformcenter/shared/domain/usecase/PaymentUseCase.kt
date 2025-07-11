package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.domain.repository.PaymentRepository

class PaymentUseCase(private val paymentRepository: PaymentRepository) {
    suspend operator fun invoke(request: PaymentRequest): String {
        return paymentRepository.generatePaymentUrl(request)
    }
}