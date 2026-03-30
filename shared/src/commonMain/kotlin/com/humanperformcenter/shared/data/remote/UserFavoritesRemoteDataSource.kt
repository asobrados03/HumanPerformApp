package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.model.user.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.Professional

interface UserFavoritesRemoteDataSource {
    suspend fun getCoaches(): Result<List<Professional>>
    suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?): Result<String>
    suspend fun getPreferredCoach(customerId: Int): Result<GetPreferredCoachResponse>
}
