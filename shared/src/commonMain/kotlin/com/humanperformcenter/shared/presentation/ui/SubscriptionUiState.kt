package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.payment.SubscriptionDto

sealed class SubscriptionUiState {
    object Idle : SubscriptionUiState()
    object Loading : SubscriptionUiState()
    data class Success(val subscription: SubscriptionDto) : SubscriptionUiState()
    data class Error(val message: String) : SubscriptionUiState()
}