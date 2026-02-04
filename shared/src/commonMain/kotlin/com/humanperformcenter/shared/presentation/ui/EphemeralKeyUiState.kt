package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.payment.EphemeralKeyDto

sealed class EphemeralKeyUiState {
    object Idle : EphemeralKeyUiState()
    object Loading : EphemeralKeyUiState()
    data class Success(val key: EphemeralKeyDto) : EphemeralKeyUiState()
    data class Error(val message: String) : EphemeralKeyUiState()
}