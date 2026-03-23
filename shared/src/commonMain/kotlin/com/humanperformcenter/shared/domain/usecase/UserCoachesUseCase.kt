package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.user.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.Professional
import com.humanperformcenter.shared.domain.repository.UserFavoritesRepository

class UserCoachesUseCase(
    private val userFavoritesRepository: UserFavoritesRepository,
) {
    suspend fun getCoaches(): Result<List<Professional>> {
        return userFavoritesRepository.getCoaches()
    }

    suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String> {
        return userFavoritesRepository.markFavorite(coachId, serviceName, userId)
    }

    suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse> {
        return userFavoritesRepository.getPreferredCoach(customerId)
    }
}
