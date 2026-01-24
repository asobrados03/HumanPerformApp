package com.humanperformcenter.ui.viewmodel.state

import com.humanperformcenter.shared.data.model.payment.PaymentMethod

sealed class PaymentMethodsUiState {
    object Loading : PaymentMethodsUiState()
    object Empty : PaymentMethodsUiState()
    data class Success(val paymentMethods: List<PaymentMethod>) : PaymentMethodsUiState()
    data class Error(val message: String) : PaymentMethodsUiState()
}