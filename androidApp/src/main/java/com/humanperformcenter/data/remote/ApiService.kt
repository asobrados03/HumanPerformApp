package com.humanperformcenter.data.remote

import com.humanperformcenter.data.remote.models.SesionesDia
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

object ApiService {
    private const val BASE_URL = "http://163.172.71.195:8085"

    suspend fun getWeeklySessions(serviceId: Int, weekStart: String): List<SesionesDia> {
        return httpClient.get("$BASE_URL/api/mobile/weekly") {
            url {
                parameters.append("service_id", serviceId.toString())
                parameters.append("week_start", weekStart)
            }
        }.body()
    }
}
