package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.user.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.Professional
import com.humanperformcenter.shared.data.remote.UserFavoritesRemoteDataSource
import com.humanperformcenter.shared.domain.repository.UserFavoritesRepository

class UserFavoritesRepositoryImpl(
    private val remote: UserFavoritesRemoteDataSource,
) : UserFavoritesRepository {
    override suspend fun getCoaches(): Result<List<Professional>> = remote.getCoaches().mapDomainError()
    override suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String> =
        remote.markFavorite(coachId, serviceName, userId).mapDomainError()

    override suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse> =
        remote.getPreferredCoach(customerId).mapDomainError()
}
