package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.user.UserStatistics
import com.humanperformcenter.shared.domain.repository.UserStatsRepository

class UserStatsUseCase(
    private val userStatsRepository: UserStatsRepository,
) {
    suspend fun getUserStats(customerId: Int): Result<UserStatistics> {
        return userStatsRepository.getUserStats(customerId)
    }
}
