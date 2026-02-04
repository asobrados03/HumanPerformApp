package com.humanperformcenter.shared.presentation.ui

sealed class ActionUiState {
    object Idle : ActionUiState()
    object Loading : ActionUiState()
    object Success : ActionUiState() // Acción completada exitosamente
    data class Error(val message: String) : ActionUiState()
}