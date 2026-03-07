package com.humanperformcenter.shared.presentation.ui

sealed class RefundUiState {
    object Idle : RefundUiState()
    object Loading : RefundUiState()
    data class Success(val productId: Int) : RefundUiState()
    data class Error(val message: String) : RefundUiState()
}

