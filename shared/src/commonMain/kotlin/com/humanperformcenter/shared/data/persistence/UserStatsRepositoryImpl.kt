package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.user.UserStatistics
import com.humanperformcenter.shared.data.remote.UserStatsRemoteDataSource
import com.humanperformcenter.shared.domain.repository.UserStatsRepository

class UserStatsRepositoryImpl(
    private val remote: UserStatsRemoteDataSource,
) : UserStatsRepository {
    override suspend fun getUserStats(customerId: Int): Result<UserStatistics> = remote.getUserStats(customerId).mapDomainError()
}
