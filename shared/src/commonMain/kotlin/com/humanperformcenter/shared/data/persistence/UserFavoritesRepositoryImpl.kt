package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.user.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.Professional
import com.humanperformcenter.shared.data.remote.UserFavoritesRemoteDataSource
import com.humanperformcenter.shared.domain.repository.UserFavoritesRepository

class UserFavoritesRepositoryImpl(
    private val remoteDataSource: UserFavoritesRemoteDataSource,
) : UserFavoritesRepository {
    override suspend fun getCoaches(): Result<List<Professional>> = remoteDataSource.getCoaches()
    override suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String> =
        remoteDataSource.markFavorite(coachId, serviceName, userId)

    override suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse> =
        remoteDataSource.getPreferredCoach(customerId)
}
