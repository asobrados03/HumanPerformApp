package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.SesionesDia
import com.humanperformcenter.shared.data.model.Session
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.SesionDiaRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.LocalDate

object SessionRepositoryImpl : SesionDiaRepository {

    override suspend fun getSessionsByWeek(serviceId: Int, weekStart: LocalDate): List<SesionesDia> {
        val client = ApiClient.httpClient

        return client.get("${ApiClient.apibaseUrl}/api/mobile/weekly") {
            parameter("service_id", serviceId)
            parameter("week_start", weekStart.toString()) // formato yyyy-MM-dd
        }.body()
    }
}
