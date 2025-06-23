package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.SesionesDia
import com.humanperformcenter.shared.data.model.Session
import kotlinx.datetime.LocalDate

interface SesionDiaRepository {
    suspend fun getSessionsByWeek(serviceId: Int, weekStart: LocalDate): List<SesionesDia>
}