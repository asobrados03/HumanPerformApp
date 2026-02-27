package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.payment.StripePaymentMethod

sealed class PaymentMethodsUiState {
    object Loading : PaymentMethodsUiState()
    object Empty : PaymentMethodsUiState()
    data class Success(
        val paymentMethods: List<StripePaymentMethod>,
        val defaultPaymentMethodId: String? = null
    ) : PaymentMethodsUiState()
    data class Error(val message: String) : PaymentMethodsUiState()
}
