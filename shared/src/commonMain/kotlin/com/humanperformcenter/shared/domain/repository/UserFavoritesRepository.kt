package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.user.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.Professional

interface UserFavoritesRepository {
    suspend fun getCoaches(): Result<List<Professional>>
    suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String>
    suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse>
}
