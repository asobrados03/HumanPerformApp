package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.shared.domain.repository.PaymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class PaymentUseCase(private val paymentRepository: PaymentRepository) {
    suspend operator fun invoke(request: PaymentRequest): String = withContext(Dispatchers.IO) {
        return@withContext paymentRepository.generatePaymentUrl(request)
    }
}