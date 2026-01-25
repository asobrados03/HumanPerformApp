package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.payment.EwalletTransaction

sealed interface EwalletUiState {
    data object Loading : EwalletUiState
    data class Success(val transactions: List<EwalletTransaction>) : EwalletUiState
    data class Error(val message: String) : EwalletUiState
}