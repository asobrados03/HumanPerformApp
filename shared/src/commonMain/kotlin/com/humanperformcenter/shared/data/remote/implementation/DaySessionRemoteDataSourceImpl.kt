package com.humanperformcenter.shared.data.remote.implementation

import com.humanperformcenter.shared.data.model.booking.BookingRequest
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.ReserveResponse
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateResponse
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.DaySessionRemoteDataSource
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate

class DaySessionRemoteDataSourceImpl(
    private val clientProvider: HttpClientProvider,
) : DaySessionRemoteDataSource {
    override suspend fun getSessionsByDay(productId: Int, weekStart: LocalDate): Result<List<DaySession>> = runCatching {
        clientProvider.apiClient.get("${clientProvider.baseUrl}/mobile/daily") {
            parameter("product_id", productId)
            parameter("date", weekStart.toString())
        }.body()
    }

    override suspend fun makeBooking(bookingRequest: BookingRequest): Result<ReserveResponse> = runCatching {
        clientProvider.apiClient.post("${clientProvider.baseUrl}/mobile/bookings") {
            contentType(ContentType.Application.Json)
            setBody(bookingRequest)
        }.body()
    }

    override suspend fun modifyBookingSession(reserveUpdateRequest: ReserveUpdateRequest): Result<ReserveUpdateResponse> = runCatching {
        clientProvider.apiClient.patch("${clientProvider.baseUrl}/mobile/bookings/${reserveUpdateRequest.booking_id}") {
            contentType(ContentType.Application.Json)
            setBody(reserveUpdateRequest)
        }.body()
    }

    override suspend fun getUserProductId(customerId: Int): Result<Int> = runCatching {
        val products = clientProvider.apiClient.get("${clientProvider.baseUrl}/mobile/users/$customerId/products").body<List<Product>>()
        products.first().id
    }

    override suspend fun getProductServiceInfo(productId: Int): Result<Int> = runCatching {
        clientProvider.apiClient.get("${clientProvider.baseUrl}/mobile/product/$productId/service-info").body()
    }

    override suspend fun getTimeslotId(serviceId: Int, dayOfWeek: String, hour: String): Result<Int> = runCatching {
        val payload = clientProvider.apiClient.get("${clientProvider.baseUrl}/mobile/timeslot-id") {
            parameter("hour", if (hour.length == 5) "$hour:00" else hour)
            parameter("service_id", serviceId)
            parameter("day_of_week", dayOfWeek)
        }.body<Map<String, Int>>()
        payload["session_timeslot_id"] ?: error("session_timeslot_id not found")
    }

    override suspend fun getHolidays(): Result<List<String>> = runCatching {
        clientProvider.apiClient.get("${clientProvider.baseUrl}/mobile/holidays").body()
    }
}
