package com.humanperformcenter.shared.presentation.ui

sealed interface StripeUiState {
    object Idle : StripeUiState
    object Loading : StripeUiState

    data class Ready(
        val clientSecret: String,
        val config: StripeCheckoutConfig
    ) : StripeUiState

    object Completed : StripeUiState
    object Canceled : StripeUiState
    data class Failed(val message: String) : StripeUiState
}

