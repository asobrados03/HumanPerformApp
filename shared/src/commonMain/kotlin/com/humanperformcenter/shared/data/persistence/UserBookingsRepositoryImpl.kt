package com.humanperformcenter.shared.data.persistence

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.UserBookingsRepository
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

object UserBookingsRepositoryImpl : UserBookingsRepository {
    private val log = logging()

    override suspend fun getUserBookings(userId: Int): Result<List<UserBooking>> = withContext(Dispatchers.IO) {
        require(userId > 0) { "customerId debe ser mayor que 0" }

        try {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user-bookings") {
                parameter("user_id", userId)
            }

            when (response.status.value) {
                200 -> {
                    val bookings = response.body<List<UserBooking>>()
                    if (bookings.isEmpty()) Result.success(emptyList()) else Result.success(bookings)
                }
                404 -> Result.success(emptyList())
                else -> {
                    log.error { "getUserBookings failed for customerId=$userId: $response" }
                    Result.failure(Exception("Error ${response.status.value}: $response"))
                }
            }
        } catch (e: IOException) {
            log.error { "Network error for customerId=$userId" }
            Result.failure(Exception("Error de red: ${e.message}", e))
        } catch (e: Exception) {
            log.error { "Unexpected error for customerId=$userId" }
            Result.failure(e)
        }
    }

    override suspend fun cancelUserBooking(bookingId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response: HttpResponse = ApiClient.apiClient.delete("${ApiClient.baseUrl}/mobile/bookings/$bookingId")
            if (response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al cancelar la reserva: código HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
