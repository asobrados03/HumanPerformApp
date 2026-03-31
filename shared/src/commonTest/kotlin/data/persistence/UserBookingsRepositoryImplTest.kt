package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.data.remote.UserBookingsRemoteDataSource
import com.humanperformcenter.shared.domain.DomainException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UserBookingsRepositoryImplTest {

    @Test
    fun get_user_bookings_when_success_propagates_expected_bookings() = runTest {
        val expected = listOf(
            UserBooking(
                id = 12,
                date = "2026-04-02",
                hour = "09:00",
                service = "Fisioterapia",
                product = "Plan mensual",
                serviceId = 3,
                productId = 2,
                coachName = "Laura",
                coachProfilePic = null,
            ),
        )
        val repository = UserBookingsRepositoryImpl(
            FakeUserBookingsRemoteDataSource(getUserBookingsResult = Result.success(expected)),
        )

        val result = repository.getUserBookings(5)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun cancel_user_booking_when_remote_error_maps_to_domain_forbidden() = runTest {
        val repository = UserBookingsRepositoryImpl(
            FakeUserBookingsRemoteDataSource(
                cancelUserBookingResult = Result.failure(IllegalStateException("HTTP 403 Forbidden")),
            ),
        )

        val result = repository.cancelUserBooking(99)

        assertTrue(result.isFailure)
        assertIs<DomainException.Forbidden>(result.exceptionOrNull())
    }

    @Test
    fun get_user_bookings_when_empty_returns_empty_list() = runTest {
        val repository = UserBookingsRepositoryImpl(
            FakeUserBookingsRemoteDataSource(getUserBookingsResult = Result.success(emptyList())),
        )

        val result = repository.getUserBookings(7)

        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrNull())
    }

    @Test
    fun get_user_bookings_when_contract_edge_nullable_ids_are_propagated() = runTest {
        val expected = listOf(
            UserBooking(
                id = 20,
                date = "2026-04-05",
                hour = "11:00",
                service = "Servicio sin vinculación",
                product = "Producto sin vinculación",
                serviceId = null,
                productId = null,
                coachName = "Sin asignar",
                coachProfilePic = null,
            ),
        )
        val repository = UserBookingsRepositoryImpl(
            FakeUserBookingsRemoteDataSource(getUserBookingsResult = Result.success(expected)),
        )

        val result = repository.getUserBookings(7)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    private class FakeUserBookingsRemoteDataSource(
        private val getUserBookingsResult: Result<List<UserBooking>> = Result.success(emptyList()),
        private val cancelUserBookingResult: Result<Unit> = Result.success(Unit),
    ) : UserBookingsRemoteDataSource {
        override suspend fun getUserBookings(userId: Int): Result<List<UserBooking>> = getUserBookingsResult
        override suspend fun cancelUserBooking(bookingId: Int): Result<Unit> = cancelUserBookingResult
    }
}
