package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.user.UserStatistics

interface UserStatsRepository {
    suspend fun getUserStats(customerId: Int): Result<UserStatistics>
}
