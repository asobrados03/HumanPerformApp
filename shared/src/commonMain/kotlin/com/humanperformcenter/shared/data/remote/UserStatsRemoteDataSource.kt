package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.model.user.UserStatistics

interface UserStatsRemoteDataSource {
    suspend fun getUserStats(customerId: Int): Result<UserStatistics>
}
