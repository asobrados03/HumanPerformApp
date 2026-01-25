package com.humanperformcenter.shared.presentation.ui

sealed class GetPreferredCoachState {
    object Idle : GetPreferredCoachState()
    object Loading : GetPreferredCoachState()
    data class Success(val coachId: Int) : GetPreferredCoachState()
    data class Error(val message: String) : GetPreferredCoachState()
}