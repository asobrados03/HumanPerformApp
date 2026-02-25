package com.humanperformcenter.shared.data.persistence

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.model.booking.BookingRequest
import com.humanperformcenter.shared.data.model.booking.ReserveResponse
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateResponse
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.WeeklyLimitsWrapper
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.DaySessionRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

object DaySessionRepositoryImpl : DaySessionRepository {
    private val log = logging()

    override suspend fun getSessionsByDay(productId: Int, weekStart: LocalDate)
    : Result<List<DaySession>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/daily") {
                parameter("product_id", productId)
                parameter("date", weekStart.toString())
            }

            if (response.status.isSuccess()) {
                val sessions = response.body<List<DaySession>>()
                Result.success(sessions)
            } else {
                // Intentamos capturar un error amigable si el backend lo envía
                Result.failure(Exception("Error al cargar sesiones: ${response.status.value}"))
            }
        } catch (e: Exception) {
            log.error { "❌ Error en getSessionsByDay: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun makeBooking(bookingRequest: BookingRequest)
    : Result<ReserveResponse> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            val response = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/reserve") {
                contentType(ContentType.Application.Json)
                setBody(bookingRequest)
            }

            if (response.status.value == 409) return@withContext Result.failure(Exception("Ya tienes una reserva a esta hora."))
            if (!response.status.isSuccess()) return@withContext Result.failure(Exception("Error de reserva: ${response.status}"))

            response.body<ReserveResponse>()
        }
    }

    override suspend fun modifyBookingSession(reserveUpdateRequest: ReserveUpdateRequest)
    : Result<ReserveUpdateResponse> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            val response = ApiClient.apiClient.put("${ApiClient.baseUrl}/mobile/update-booking") {
                contentType(ContentType.Application.Json)
                setBody(reserveUpdateRequest)
            }

            if (!response.status.isSuccess()) return@withContext Result.failure(Exception("Error al actualizar reserva: ${response.status}"))

            response.body<ReserveUpdateResponse>()
        }
    }

    override suspend fun getUserProductId(customerId: Int)
    : Result<Int> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user-product") {
                parameter("user_id", customerId)
            }
            val json = response.body<Map<String, Int>>()
            json["product_id"] ?: throw Exception("No se encontró el product_id")
        }
    }

    override suspend fun getProductServiceInfo(productId: Int)
    : Result<Int> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            // Construimos la URL inyectando el productId en el path
            val response = ApiClient.apiClient.get(
                "${ApiClient.baseUrl}/mobile/product/$productId/service-info"
            )

            if (!response.status.isSuccess()) {
                throw Exception("Error de servidor: ${response.status}")
            }

            response.body<Int>()
        }
    }

    override suspend fun getTimeslotId(
        serviceId: Int,
        dayOfWeek: String,
        hour: String
    ): Result<Int> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            val formattedHour = if (hour.length == 5) "$hour:00" else hour
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/timeslot-id") {
                parameter("hour", formattedHour)
                parameter("service_id", serviceId)
                parameter("day_of_week", dayOfWeek)
            }
            val json = response.body<Map<String, Int>>()
            json["session_timeslot_id"] ?: throw Exception("ID de franja horaria no válido")
        }
    }

    override suspend fun getUserWeeklyLimit(userId: Int)
    : Result<WeeklyLimitsWrapper> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user-weekly-limit") {
                parameter("user_id", userId)
            }
            if (!response.status.isSuccess()) throw Exception("Error de servidor: ${response.status}")
            response.body<WeeklyLimitsWrapper>()
        }
    }

    override suspend fun getHolidays(): Result<List<String>> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/holidays")
            if (!response.status.isSuccess()) throw Exception("Error al cargar festivos")
            response.body<List<String>>()
        }
    }
}
