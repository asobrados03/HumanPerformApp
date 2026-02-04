package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.payment.GetStripeCustomerResponse

sealed class CustomerUiState {
    object Idle : CustomerUiState()
    object Loading : CustomerUiState()
    data class Success(val customer: GetStripeCustomerResponse) : CustomerUiState()
    data class Error(val message: String) : CustomerUiState()
}