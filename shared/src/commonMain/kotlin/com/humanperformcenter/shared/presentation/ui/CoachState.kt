package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.user.Professional

sealed class CoachState {
    object Idle : CoachState()
    object Loading : CoachState()
    data class Success(val coaches: List<Professional>) : CoachState()
    data class Error(val message: String) : CoachState()
}