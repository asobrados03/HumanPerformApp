package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.PaymentRequest

interface PaymentRepository {
    suspend fun generatePaymentUrl(request: PaymentRequest) : String
}