package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.ReservaRequest
import com.humanperformcenter.shared.data.model.ReservaResponse
import com.humanperformcenter.shared.data.model.SesionesDia
import com.humanperformcenter.shared.data.model.Session
import kotlinx.datetime.LocalDate

interface SesionDiaRepository {
    suspend fun getSessionsByDay(serviceId: Int, date: LocalDate): List<SesionesDia>
    suspend fun reservarSesion(reservaRequest: ReservaRequest): ReservaResponse
    suspend fun getUserProductId(): Int
    suspend fun getPreferredCoach(customerId: Int, serviceId: Int): Int?
    suspend fun getTimeslotId(hora: String): Int
}