package com.humanperformcenter.shared.presentation.ui

sealed interface WalletBalanceUiState {
    data object Idle : WalletBalanceUiState
    data object Loading : WalletBalanceUiState
    data class Success(val amount: Double) : WalletBalanceUiState
    data class Error(val message: String) : WalletBalanceUiState
}
