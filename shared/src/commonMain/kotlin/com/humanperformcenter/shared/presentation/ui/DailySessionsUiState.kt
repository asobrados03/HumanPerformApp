package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.booking.DaySession
import kotlinx.datetime.LocalDate

data class SessionsRequestContext(
    val productId: Int,
    val date: LocalDate
)

sealed class DailySessionsUiState {
    object Idle : DailySessionsUiState()
    data class Loading(val context: SessionsRequestContext) : DailySessionsUiState()
    data class Success(
        val sessions: List<DaySession>,
        val context: SessionsRequestContext
    ) : DailySessionsUiState()
    data class Error(
        val message: String,
        val context: SessionsRequestContext
    ) : DailySessionsUiState()
    data class Empty(val context: SessionsRequestContext) : DailySessionsUiState()
}
