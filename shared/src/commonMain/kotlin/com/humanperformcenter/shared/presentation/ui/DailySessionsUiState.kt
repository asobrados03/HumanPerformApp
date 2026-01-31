package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.booking.DaySession

sealed class DailySessionsUiState {
    object Idle : DailySessionsUiState()
    object Loading : DailySessionsUiState()
    data class Success(val sessions: List<DaySession>) : DailySessionsUiState()
    data class Error(val message: String) : DailySessionsUiState()
    object Empty : DailySessionsUiState() // Estado específico para "No hay horarios"
}