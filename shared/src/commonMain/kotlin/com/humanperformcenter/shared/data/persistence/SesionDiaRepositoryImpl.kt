package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.ReservaRequest
import com.humanperformcenter.shared.data.model.ReservaResponse
import com.humanperformcenter.shared.data.model.SesionesDia
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.SesionDiaRepository
import com.humanperformcenter.shared.domain.storage.SecureStorage
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.utils.EmptyContent.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

object SesionDiaRepositoryImpl : SesionDiaRepository {

    override suspend fun getSessionsByDay(serviceId: Int, weekStart: LocalDate): List<SesionesDia> {
        val client = ApiClient.httpClient

        return client.get("${ApiClient.baseUrl}/mobile/daily") {
            parameter("service_id", serviceId)
            parameter("date", weekStart.toString()) // formato yyyy-MM-dd
        }.body()
    }

    override suspend fun reservarSesion(request: ReservaRequest): ReservaResponse {
        val response = ApiClient.httpClient.post("${ApiClient.baseUrl}/mobile/reserve") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(request)
        }
        return response.body()
    }

    override suspend fun getUserProductId(customerId: Int): Int {
        val client = ApiClient.httpClient
        val token = SecureStorage.getAccessToken()

        try {
            val response = client.get("${ApiClient.baseUrl}/mobile/user-product?user_id=$customerId") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token") // aún puedes pasarlo si el backend lo necesita
                }
            }

            if (!response.status.isSuccess()) {
                throw IllegalStateException("Error HTTP: ${response.status}")
            }

            val json = response.body<Map<String, Int>>()
            val productId = json["product_id"]
            println("🧾 Product ID recibido del backend: $productId")

            return productId ?: throw IllegalStateException("No se encontró el product_id en la respuesta")
        } catch (e: Exception) {
            println("❌ Error al obtener el product_id: ${e.message}")
            throw e
        }
    }


    override suspend fun getPreferredCoach(customerId: Int, serviceId: Int): Int? {
        val response = ApiClient.httpClient.get("${ApiClient.baseUrl}/mobile/preferred-coach") {
            parameter("customer_id", customerId)
            parameter("service_id", serviceId)
        }

        val json = response.body<JsonObject>()
        return json["coach_id"]?.jsonPrimitive?.intOrNull
    }
    override suspend fun getTimeslotId(hour: String): Int {
        val client = ApiClient.httpClient
        val token = SecureStorage.getAccessToken()
        val formattedHour = if (hour.length == 5) "$hour:00" else hour

        val response = client.get("${ApiClient.baseUrl}/mobile/timeslot-id") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            parameter("hour", formattedHour)
        }

        if (!response.status.isSuccess()) {
            throw IllegalStateException("No se encontró el timeslot_id para la hora $formattedHour")
        }

        val json = response.body<Map<String, Int>>()
        return json["session_timeslot_id"] ?: throw IllegalStateException("Respuesta inválida")
    }

}
