package com.humanperformcenter.shared.presentation.ui

sealed class StripeUiState {
    object Idle : StripeUiState()
    data class Ready(val clientSecret: String, val config: StripeCheckoutConfig) : StripeUiState()
    data class Result(val result: Any) : StripeUiState()
    data class Error(val message: String) : StripeUiState()
}
