package com.humanperformcenter.shared.presentation.ui

import com.humanperformcenter.shared.data.model.user.UserStatistics

sealed interface UserStatsState {
    data object Loading : UserStatsState
    data class Success(val stats: UserStatistics) : UserStatsState
    data class Error(val message: String) : UserStatsState
}