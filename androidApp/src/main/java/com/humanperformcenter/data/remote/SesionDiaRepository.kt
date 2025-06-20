package com.humanperformcenter.data.remote

import com.humanperformcenter.data.remote.ApiService
import com.humanperformcenter.data.remote.models.SesionesDia

class SesionDiaRepository {
    suspend fun getWeeklySessions(serviceId: Int, weekStart: String): List<SesionesDia> {
        return ApiService.getWeeklySessions(serviceId, weekStart)
    }
}
