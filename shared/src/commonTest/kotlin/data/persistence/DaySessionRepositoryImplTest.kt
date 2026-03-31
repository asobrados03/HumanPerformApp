package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.booking.BookingRequest
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.ReserveResponse
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateResponse
import com.humanperformcenter.shared.data.remote.DaySessionRemoteDataSource
import com.humanperformcenter.shared.domain.DomainException
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DaySessionRepositoryImplTest {

    @Test
    fun get_sessions_by_day_when_success_propagates_expected_sessions() = runTest {
        val expected = listOf(
            DaySession(
                productId = 10,
                date = "2026-03-31",
                hour = "10:00",
                coachId = 8,
                coachName = "Coach A",
                booked = 2,
                capacity = 8,
            ),
        )
        val repository = DaySessionRepositoryImpl(
            FakeDaySessionRemoteDataSource(getSessionsByDayResult = Result.success(expected)),
        )

        val result = repository.getSessionsByDay(productId = 10, weekStart = LocalDate.parse("2026-03-30"))

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun get_sessions_by_day_when_remote_error_maps_to_domain_server() = runTest {
        val repository = DaySessionRepositoryImpl(
            FakeDaySessionRemoteDataSource(
                getSessionsByDayResult = Result.failure(IllegalStateException("HTTP 500 Internal Server Error")),
            ),
        )

        val result = repository.getSessionsByDay(productId = 2, weekStart = LocalDate.parse("2026-03-30"))

        assertTrue(result.isFailure)
        assertIs<DomainException.Server>(result.exceptionOrNull())
    }

    @Test
    fun get_holidays_when_empty_returns_empty_list() = runTest {
        val repository = DaySessionRepositoryImpl(
            FakeDaySessionRemoteDataSource(getHolidaysResult = Result.success(emptyList())),
        )

        val result = repository.getHolidays()

        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrNull())
    }

    @Test
    fun modify_booking_session_when_inconsistent_state_is_propagated_without_mutation() = runTest {
        val request = ReserveUpdateRequest(
            booking_id = 999,
            new_coach_id = -1,
            new_service_id = 1,
            new_product_id = 5,
            new_session_timeslot_id = 7,
            new_start_date = "2026-03-31",
        )
        val expected = ReserveUpdateResponse(message = "updated with warnings")
        val repository = DaySessionRepositoryImpl(
            FakeDaySessionRemoteDataSource(modifyBookingSessionResult = Result.success(expected)),
        )

        val result = repository.modifyBookingSession(request)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    private class FakeDaySessionRemoteDataSource(
        private val getSessionsByDayResult: Result<List<DaySession>> = Result.success(emptyList()),
        private val makeBookingResult: Result<ReserveResponse> = Result.success(ReserveResponse("ok", 1)),
        private val modifyBookingSessionResult: Result<ReserveUpdateResponse> = Result.success(ReserveUpdateResponse("ok")),
        private val getUserProductIdResult: Result<Int> = Result.success(1),
        private val getProductServiceInfoResult: Result<Int> = Result.success(1),
        private val getTimeslotIdResult: Result<Int> = Result.success(1),
        private val getHolidaysResult: Result<List<String>> = Result.success(listOf("2026-12-25")),
    ) : DaySessionRemoteDataSource {
        override suspend fun getSessionsByDay(productId: Int, weekStart: LocalDate): Result<List<DaySession>> =
            getSessionsByDayResult

        override suspend fun makeBooking(bookingRequest: BookingRequest): Result<ReserveResponse> = makeBookingResult

        override suspend fun modifyBookingSession(reserveUpdateRequest: ReserveUpdateRequest): Result<ReserveUpdateResponse> =
            modifyBookingSessionResult

        override suspend fun getUserProductId(customerId: Int): Result<Int> = getUserProductIdResult
        override suspend fun getProductServiceInfo(productId: Int): Result<Int> = getProductServiceInfoResult
        override suspend fun getTimeslotId(serviceId: Int, dayOfWeek: String, hour: String): Result<Int> = getTimeslotIdResult
        override suspend fun getHolidays(): Result<List<String>> = getHolidaysResult
    }
}
