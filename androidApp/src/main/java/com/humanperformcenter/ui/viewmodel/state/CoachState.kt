package com.humanperformcenter.ui.viewmodel.state

import com.humanperformcenter.shared.data.model.Professional

sealed class CoachState {
    object Idle : CoachState()
    object Loading : CoachState()
    data class Success(val coaches: List<Professional>) : CoachState()
    data class Error(val message: String) : CoachState()
}