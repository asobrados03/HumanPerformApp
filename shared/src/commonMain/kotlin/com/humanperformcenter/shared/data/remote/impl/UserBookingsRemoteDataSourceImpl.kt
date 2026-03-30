package com.humanperformcenter.shared.data.remote.impl

import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.UserBookingsRemoteDataSource
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class UserBookingsRemoteDataSourceImpl(
    private val clientProvider: HttpClientProvider,
) : UserBookingsRemoteDataSource {
    override suspend fun getUserBookings(userId: Int): Result<List<UserBooking>> = runCatching {
        clientProvider.apiClient.get("${clientProvider.baseUrl}/mobile/user-bookings") {
            parameter("user_id", userId)
        }.body()
    }

    override suspend fun cancelUserBooking(bookingId: Int): Result<Unit> = runCatching {
        clientProvider.apiClient.delete("${clientProvider.baseUrl}/mobile/bookings/$bookingId")
        Unit
    }
}
