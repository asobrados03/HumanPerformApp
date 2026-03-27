package com.humanperformcenter.shared.presentation

import app.cash.turbine.test
import com.humanperformcenter.shared.data.model.booking.BookingRequest
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.ReserveResponse
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateResponse
import com.humanperformcenter.shared.domain.booking.BookingDomainException
import com.humanperformcenter.shared.domain.repository.DaySessionRepository
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase
import com.humanperformcenter.shared.presentation.ui.DailySessionsUiState
import com.humanperformcenter.shared.presentation.ui.SessionsRequestContext
import com.humanperformcenter.shared.presentation.viewmodel.DaySessionViewModel
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DaySessionViewModelTest {

    private class FakeDaySessionRepository(
        private val sessionsResult: Result<List<DaySession>> = Result.success(emptyList()),
        private val timeslotResult: Result<Int> = Result.success(77),
        private val bookingResult: Result<ReserveResponse> = Result.success(ReserveResponse("ok", 1)),
        private val modifyResult: Result<ReserveUpdateResponse> = Result.success(ReserveUpdateResponse("ok")),
        private val serviceIdResult: Result<Int> = Result.success(10),
        private val holidaysResult: Result<List<String>> = Result.success(emptyList())
    ) : DaySessionRepository {
        override suspend fun getSessionsByDay(productId: Int, weekStart: LocalDate): Result<List<DaySession>> = sessionsResult
        override suspend fun makeBooking(bookingRequest: BookingRequest): Result<ReserveResponse> = bookingResult
        override suspend fun modifyBookingSession(reserveUpdateRequest: ReserveUpdateRequest): Result<ReserveUpdateResponse> = modifyResult
        override suspend fun getUserProductId(customerId: Int): Result<Int> = Result.success(1)
        override suspend fun getProductServiceInfo(productId: Int): Result<Int> = serviceIdResult
        override suspend fun getTimeslotId(serviceId: Int, dayOfWeek: String, hour: String): Result<Int> = timeslotResult
        override suspend fun getHolidays(): Result<List<String>> = holidaysResult
    }

    @Test
    fun fetchAvailableSessions_handlesSuccessEmptyAndError() = runTest {
        val date = LocalDate.parse("2026-03-27")
        val ctx = SessionsRequestContext(productId = 1, date = date)
        val matching = DaySession(1, "2026-03-27", "10:00", 5, "Ana", 0, 1)
        val other = DaySession(2, "2026-03-27", "10:00", 6, "Juan", 0, 1)

        val successVm = DaySessionViewModel(
            DaySessionUseCase(FakeDaySessionRepository(sessionsResult = Result.success(listOf(matching, other))))
        )
        successVm.sessions.test {
            assertEquals(DailySessionsUiState.Idle, awaitItem())
            successVm.fetchAvailableSessions(1, date)
            assertEquals(DailySessionsUiState.Loading(ctx), awaitItem())
            assertEquals(DailySessionsUiState.Success(listOf(matching), ctx), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        val emptyVm = DaySessionViewModel(
            DaySessionUseCase(FakeDaySessionRepository(sessionsResult = Result.success(listOf(other))))
        )
        emptyVm.sessions.test {
            assertEquals(DailySessionsUiState.Idle, awaitItem())
            emptyVm.fetchAvailableSessions(1, date)
            assertEquals(DailySessionsUiState.Loading(ctx), awaitItem())
            assertEquals(DailySessionsUiState.Empty(ctx), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        val errorVm = DaySessionViewModel(
            DaySessionUseCase(FakeDaySessionRepository(sessionsResult = Result.failure(IllegalStateException("fallo"))))
        )
        errorVm.sessions.test {
            assertEquals(DailySessionsUiState.Idle, awaitItem())
            errorVm.fetchAvailableSessions(1, date)
            assertEquals(DailySessionsUiState.Loading(ctx), awaitItem())
            assertEquals(DailySessionsUiState.Error("fallo", ctx), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAvailableCoachesForHour_filtersByHourAndCapacity() = runTest {
        val date = LocalDate.parse("2026-03-27")
        val available = DaySession(1, "2026-03-27", "10:00", 5, "Ana", 0, 1)
        val full = DaySession(1, "2026-03-27", "10:00", 6, "Juan", 1, 1)
        val otherHour = DaySession(1, "2026-03-27", "11:00", 7, "Marta", 0, 1)
        val vm = DaySessionViewModel(
            DaySessionUseCase(FakeDaySessionRepository(sessionsResult = Result.success(listOf(available, full, otherHour))))
        )

        vm.fetchAvailableSessions(1, date)

        assertEquals(listOf(available), vm.getAvailableCoachesForHour("10:00"))
        assertTrue(vm.getAvailableCoachesForHour("13:00").isEmpty())
    }

    @Test
    fun makeBooking_and_modifyBooking_coverSyncAndAsyncPaths() = runTest {
        val vm = DaySessionViewModel(DaySessionUseCase(FakeDaySessionRepository()))

        val booked = vm.makeBooking(1, 2, 3, 4, "monday", 5, "2026-03-27", "10:00")
        assertTrue(booked)
        assertNull(vm.bookingErrorMessage.value)

        var asyncBookingResult: Boolean? = null
        vm.makeBookingAsync(1, 2, 3, 4, "monday", 5, "2026-03-27", "10:00") { asyncBookingResult = it }
        assertEquals(true, asyncBookingResult)

        val modified = vm.modifyBookingSession(10, 2, 3, 4, "monday", "2026-03-28", "11:00")
        assertTrue(modified)
        assertNull(vm.bookingErrorMessage.value)

        var asyncModifyResult: Boolean? = null
        vm.modifyBookingSessionAsync(10, 2, 3, 4, "monday", "2026-03-28", "11:00") { asyncModifyResult = it }
        assertEquals(true, asyncModifyResult)
    }

    @Test
    fun makeBooking_and_modifyBooking_handleTimeslotAndBookingErrors() = runTest {
        val timeslotError = DaySessionViewModel(
            DaySessionUseCase(
                FakeDaySessionRepository(timeslotResult = Result.failure(BookingDomainException.DuplicateBooking))
            )
        )

        val bookingResult = timeslotError.makeBooking(1, 2, 3, 4, "monday", 5, "2026-03-27", "10:00")
        assertEquals(false, bookingResult)
        assertEquals("Ya tienes una reserva a esta hora.", timeslotError.bookingErrorMessage.value)

        val genericErrorVm = DaySessionViewModel(
            DaySessionUseCase(
                FakeDaySessionRepository(bookingResult = Result.failure(IllegalStateException("whatever")))
            )
        )
        val failedBooking = genericErrorVm.makeBooking(1, 2, 3, 4, "monday", 5, "2026-03-27", "10:00")
        assertEquals(false, failedBooking)
        assertEquals(
            "No se pudo completar la reserva. Inténtalo de nuevo más tarde.",
            genericErrorVm.bookingErrorMessage.value
        )

        val modifyErrorVm = DaySessionViewModel(
            DaySessionUseCase(
                FakeDaySessionRepository(modifyResult = Result.failure(BookingDomainException.WeeklyLimitExceeded))
            )
        )
        val modified = modifyErrorVm.modifyBookingSession(10, 2, 3, 4, "monday", "2026-03-28", "11:00")
        assertEquals(false, modified)
        assertEquals("Has alcanzado tu máximo semanal.", modifyErrorVm.bookingErrorMessage.value)

        modifyErrorVm.clearBookingErrorMessage()
        assertNull(modifyErrorVm.bookingErrorMessage.value)
    }

    @Test
    fun fetchServiceIdAsync_clearSessions_and_fetchHolidays_work() = runTest {
        val vm = DaySessionViewModel(
            DaySessionUseCase(
                FakeDaySessionRepository(
                    serviceIdResult = Result.success(42),
                    holidaysResult = Result.success(listOf("2026-12-25", "2026-01-01"))
                )
            )
        )

        assertEquals(42, vm.fetchServiceIdForProduct(9))

        var serviceIdAsync: Int? = null
        vm.fetchServiceIdForProductAsync(9) { serviceIdAsync = it }
        assertEquals(42, serviceIdAsync)

        vm.fetchAvailableSessions(1, LocalDate.parse("2026-03-27"))
        vm.clearSessions()
        assertEquals(DailySessionsUiState.Idle, vm.sessions.value)

        vm.fetchHolidays()
        assertEquals(
            listOf(LocalDate.parse("2026-12-25"), LocalDate.parse("2026-01-01")),
            vm.holidays.value
        )
    }
}
