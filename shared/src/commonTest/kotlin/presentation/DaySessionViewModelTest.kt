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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DaySessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

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

    private fun buildViewModel(repository: FakeDaySessionRepository = FakeDaySessionRepository()) =
        DaySessionViewModel(DaySessionUseCase(repository))

    @Test
    fun fetchAvailableSessions_when_success_with_matching_data_emits_loading_then_success() = runTest(testDispatcher.scheduler) {
        val date = LocalDate.parse("2026-03-27")
        val ctx = SessionsRequestContext(productId = 1, date = date)
        val matching = DaySession(1, "2026-03-27", "10:00", 5, "Ana", 0, 1)
        val other = DaySession(2, "2026-03-27", "10:00", 6, "Juan", 0, 1)
        val viewModel = buildViewModel(
            FakeDaySessionRepository(sessionsResult = Result.success(listOf(matching, other)))
        )

        viewModel.sessions.test {
            assertEquals(DailySessionsUiState.Idle, awaitItem())
            viewModel.fetchAvailableSessions(1, date)
            assertEquals(DailySessionsUiState.Loading(ctx), awaitItem())
            assertEquals(DailySessionsUiState.Success(listOf(matching), ctx), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun fetchAvailableSessions_when_no_coach_has_capacity_emits_empty() = runTest(testDispatcher.scheduler) {
        val date = LocalDate.parse("2026-03-27")
        val ctx = SessionsRequestContext(productId = 1, date = date)
        val other = DaySession(2, "2026-03-27", "10:00", 6, "Juan", 1, 1)
        val viewModel = buildViewModel(
            FakeDaySessionRepository(sessionsResult = Result.success(listOf(other)))
        )

        viewModel.sessions.test {
            assertEquals(DailySessionsUiState.Idle, awaitItem())
            viewModel.fetchAvailableSessions(1, date)
            assertEquals(DailySessionsUiState.Loading(ctx), awaitItem())
            assertEquals(DailySessionsUiState.Empty(ctx), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun fetchAvailableSessions_when_repository_fails_emits_error() = runTest(testDispatcher.scheduler) {
        val date = LocalDate.parse("2026-03-27")
        val ctx = SessionsRequestContext(productId = 1, date = date)
        val viewModel = buildViewModel(
            FakeDaySessionRepository(sessionsResult = Result.failure(IllegalStateException("fallo")))
        )

        viewModel.sessions.test {
            assertEquals(DailySessionsUiState.Idle, awaitItem())
            viewModel.fetchAvailableSessions(1, date)
            assertEquals(DailySessionsUiState.Loading(ctx), awaitItem())
            assertEquals(DailySessionsUiState.Error("fallo", ctx), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAvailableCoachesForHour_returns_only_available_coaches_for_requested_hour() = runTest(testDispatcher.scheduler) {
        val date = LocalDate.parse("2026-03-27")
        val available = DaySession(1, "2026-03-27", "10:00", 5, "Ana", 0, 1)
        val full = DaySession(1, "2026-03-27", "10:00", 6, "Juan", 1, 1)
        val otherHour = DaySession(1, "2026-03-27", "11:00", 7, "Marta", 0, 1)
        val viewModel = buildViewModel(
            FakeDaySessionRepository(sessionsResult = Result.success(listOf(available, full, otherHour)))
        )

        viewModel.fetchAvailableSessions(1, date)
        advanceUntilIdle()

        assertEquals(listOf(available), viewModel.getAvailableCoachesForHour("10:00"))
        assertTrue(viewModel.getAvailableCoachesForHour("13:00").isEmpty())
    }

    @Test
    fun makeBooking_and_modifyBooking_when_success_return_true_and_keep_error_null() = runTest(testDispatcher.scheduler) {
        val viewModel = buildViewModel()

        val booked = viewModel.makeBooking(1, 2, 3, 4, "monday", 5, "2026-03-27", "10:00")
        val modified = viewModel.modifyBookingSession(10, 2, 3, 4, "monday", "2026-03-28", "11:00")

        assertTrue(booked)
        assertTrue(modified)
        assertNull(viewModel.bookingErrorMessage.value)
    }

    @Test
    fun makeBookingAsync_and_modifyBookingAsync_when_success_invoke_callbacks_with_true() = runTest(testDispatcher.scheduler) {
        val viewModel = buildViewModel()
        var asyncBookingResult: Boolean? = null
        var asyncModifyResult: Boolean? = null

        viewModel.makeBookingAsync(1, 2, 3, 4, "monday", 5, "2026-03-27", "10:00") { asyncBookingResult = it }
        viewModel.modifyBookingSessionAsync(10, 2, 3, 4, "monday", "2026-03-28", "11:00") { asyncModifyResult = it }
        advanceUntilIdle()

        assertEquals(true, asyncBookingResult)
        assertEquals(true, asyncModifyResult)
    }

    @Test
    fun makeBooking_when_timeslot_fails_with_domain_error_maps_to_friendly_message() = runTest(testDispatcher.scheduler) {
        val viewModel = buildViewModel(
            FakeDaySessionRepository(timeslotResult = Result.failure(BookingDomainException.DuplicateBooking))
        )

        val booked = viewModel.makeBooking(1, 2, 3, 4, "monday", 5, "2026-03-27", "10:00")

        assertEquals(false, booked)
        assertEquals("Ya tienes una reserva a esta hora.", viewModel.bookingErrorMessage.value)
    }

    @Test
    fun makeBooking_when_booking_fails_with_generic_error_maps_to_generic_message() = runTest(testDispatcher.scheduler) {
        val viewModel = buildViewModel(
            FakeDaySessionRepository(bookingResult = Result.failure(IllegalStateException("whatever")))
        )

        val booked = viewModel.makeBooking(1, 2, 3, 4, "monday", 5, "2026-03-27", "10:00")

        assertEquals(false, booked)
        assertEquals(
            "No se pudo completar la reserva. Inténtalo de nuevo más tarde.",
            viewModel.bookingErrorMessage.value
        )
    }

    @Test
    fun modifyBookingSession_when_weekly_limit_exceeded_maps_to_friendly_message_and_can_clear_error() = runTest(testDispatcher.scheduler) {
        val viewModel = buildViewModel(
            FakeDaySessionRepository(modifyResult = Result.failure(BookingDomainException.WeeklyLimitExceeded))
        )

        val modified = viewModel.modifyBookingSession(10, 2, 3, 4, "monday", "2026-03-28", "11:00")

        assertEquals(false, modified)
        assertEquals("Has alcanzado tu máximo semanal.", viewModel.bookingErrorMessage.value)

        viewModel.clearBookingErrorMessage()
        assertNull(viewModel.bookingErrorMessage.value)
    }

    @Test
    fun fetchServiceIdForProduct_and_fetchServiceIdForProductAsync_return_repository_value() = runTest(testDispatcher.scheduler) {
        val viewModel = buildViewModel(FakeDaySessionRepository(serviceIdResult = Result.success(42)))
        var serviceIdAsync: Int? = null

        val serviceId = viewModel.fetchServiceIdForProduct(9)
        viewModel.fetchServiceIdForProductAsync(9) { serviceIdAsync = it }
        advanceUntilIdle()

        assertEquals(42, serviceId)
        assertEquals(42, serviceIdAsync)
    }

    @Test
    fun clearSessions_and_fetchHolidays_reset_sessions_and_parse_holidays() = runTest(testDispatcher.scheduler) {
        val viewModel = buildViewModel(
            FakeDaySessionRepository(holidaysResult = Result.success(listOf("2026-12-25", "2026-01-01")))
        )

        viewModel.fetchAvailableSessions(1, LocalDate.parse("2026-03-27"))
        viewModel.clearSessions()
        viewModel.fetchHolidays()
        advanceUntilIdle()

        assertEquals(DailySessionsUiState.Idle, viewModel.sessions.value)
        assertEquals(
            listOf(LocalDate.parse("2026-12-25"), LocalDate.parse("2026-01-01")),
            viewModel.holidays.value
        )
    }
}
