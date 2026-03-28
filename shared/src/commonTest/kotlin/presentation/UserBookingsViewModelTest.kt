package com.humanperformcenter.shared.presentation

import app.cash.turbine.test
import com.humanperformcenter.shared.SessionNotificationManager
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.domain.repository.UserBookingsRepository
import com.humanperformcenter.shared.domain.usecase.UserBookingsUseCase
import com.humanperformcenter.shared.presentation.ui.FetchUserBookingsState
import com.humanperformcenter.shared.presentation.viewmodel.UserBookingsViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserBookingsViewModelTest {

    private class FakeUserBookingsRepository(
        private val bookingsResult: Result<List<UserBooking>> = Result.success(emptyList()),
        private val cancelResult: Result<Unit> = Result.success(Unit)
    ) : UserBookingsRepository {
        var getBookingsCalls = 0
        override suspend fun getUserBookings(userId: Int): Result<List<UserBooking>> {
            getBookingsCalls++
            return bookingsResult
        }

        override suspend fun cancelUserBooking(bookingId: Int): Result<Unit> = cancelResult
    }

    private class FakeNotificationManager : SessionNotificationManager {
        var canceledBookingId: Int? = null

        override fun cancelNotification(bookingId: Int) {
            canceledBookingId = bookingId
        }
    }

    private fun buildViewModel(
        repository: FakeUserBookingsRepository = FakeUserBookingsRepository(),
        notificationManager: FakeNotificationManager = FakeNotificationManager()
    ) = UserBookingsViewModel(UserBookingsUseCase(repository), notificationManager)

    @Test
    fun fetchuserbookings_when_success_emits_loading_then_success() = runTest {
        val booking = UserBooking(1, "2026-03-20", "10:00", "PT", "Pack", 1, 2, "Coach", null)
        val viewModel = buildViewModel(
            repository = FakeUserBookingsRepository(bookingsResult = Result.success(listOf(booking)))
        )

        viewModel.userBookings.test {
            assertEquals(FetchUserBookingsState.Loading, awaitItem())
            viewModel.fetchUserBookings(1)
            assertEquals(FetchUserBookingsState.Success(listOf(booking)), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun fetchuserbookings_when_failure_without_message_emits_default_error() = runTest {
        val viewModel = buildViewModel(
            repository = FakeUserBookingsRepository(bookingsResult = Result.failure(IllegalStateException()))
        )

        viewModel.userBookings.test {
            assertEquals(FetchUserBookingsState.Loading, awaitItem())
            viewModel.fetchUserBookings(1)
            assertEquals(FetchUserBookingsState.Error("Ocurrió un error inesperado"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun canceluserbooking_when_success_cancels_notification_and_refreshes_bookings() = runTest {
        val repository = FakeUserBookingsRepository(bookingsResult = Result.success(emptyList()))
        val notifications = FakeNotificationManager()
        val viewModel = buildViewModel(repository, notifications)

        viewModel.userBookings.test {
            assertEquals(FetchUserBookingsState.Loading, awaitItem())
            viewModel.cancelUserBooking(bookingId = 99, currentUser = sampleUser(id = 7))
            assertEquals(FetchUserBookingsState.Loading, awaitItem())
            assertEquals(FetchUserBookingsState.Success(emptyList()), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(99, notifications.canceledBookingId)
        assertEquals(1, repository.getBookingsCalls)
    }

    @Test
    fun canceluserbooking_when_failure_does_not_refresh_or_cancel_notification() = runTest {
        val repository = FakeUserBookingsRepository(cancelResult = Result.failure(IllegalStateException("fallo")))
        val notifications = FakeNotificationManager()
        val viewModel = buildViewModel(repository, notifications)

        viewModel.cancelUserBooking(bookingId = 99, currentUser = sampleUser())

        assertEquals(null, notifications.canceledBookingId)
        assertEquals(0, repository.getBookingsCalls)
    }

    private fun sampleUser(id: Int = 1): User = User(
        id = id,
        fullName = "Test User",
        email = "test@example.com",
        phone = "600000000",
        sex = "M",
        dateOfBirth = "1990-01-01",
        postcode = 28001,
        postAddress = "Street 1",
        dni = "12345678A",
        profilePictureName = null
    )
}
