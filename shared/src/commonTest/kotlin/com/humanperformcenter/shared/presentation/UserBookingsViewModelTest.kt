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
        initialBookingsByUser: Map<Int, List<UserBooking>> = emptyMap(),
        private val failGetWithoutMessage: Boolean = false,
        private val failCancelWithMessage: String? = null
    ) : UserBookingsRepository {
        private val bookingsByUser = initialBookingsByUser.mapValues { it.value.toMutableList() }.toMutableMap()
        var getBookingsCalls = 0
        override suspend fun getUserBookings(userId: Int): Result<List<UserBooking>> {
            getBookingsCalls++
            if (failGetWithoutMessage) return Result.failure(IllegalStateException())
            return Result.success(bookingsByUser[userId].orEmpty().toList())
        }

        override suspend fun cancelUserBooking(bookingId: Int): Result<Unit> {
            failCancelWithMessage?.let { return Result.failure(IllegalStateException(it)) }
            bookingsByUser.values.forEach { bookings ->
                bookings.removeAll { it.id == bookingId }
            }
            return Result.success(Unit)
        }
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
    fun fetchUserBookings_when_success_emits_loading_then_success() = runTest {
        // Arrange
        val booking = UserBooking(1, "2026-03-20", "10:00", "PT", "Pack", 1, 2, "Coach", null)
        val viewModel = buildViewModel(
        // Act
            repository = FakeUserBookingsRepository(initialBookingsByUser = mapOf(1 to listOf(booking)))
        )

        viewModel.userBookings.test {
        // Assert
            assertEquals(FetchUserBookingsState.Loading, awaitItem())
            viewModel.fetchUserBookings(1)
            assertEquals(FetchUserBookingsState.Success(listOf(booking)), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun fetchUserBookings_when_failure_without_message_emits_default_error() = runTest {
        // Arrange
        val viewModel = buildViewModel(
        // Act
            repository = FakeUserBookingsRepository(failGetWithoutMessage = true)
        )

        viewModel.userBookings.test {
        // Assert
            assertEquals(FetchUserBookingsState.Loading, awaitItem())
            viewModel.fetchUserBookings(1)
            assertEquals(FetchUserBookingsState.Error("Ocurrió un error inesperado"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun cancelUserBooking_when_success_cancels_notification_and_refreshes_bookings() = runTest {
        // Arrange
        val existing = UserBooking(99, "2026-03-20", "10:00", "PT", "Pack", 1, 2, "Coach", null)
        val repository = FakeUserBookingsRepository(initialBookingsByUser = mapOf(7 to listOf(existing)))
        val notifications = FakeNotificationManager()
        val viewModel = buildViewModel(repository, notifications)

        // Act
        viewModel.userBookings.test {
        // Assert
            assertEquals(FetchUserBookingsState.Loading, awaitItem()) // Loading inicial

            viewModel.cancelUserBooking(bookingId = 99, currentUser = sampleUser(id = 7))
            // No esperamos un segundo Loading porque StateFlow deduplica
            assertEquals(FetchUserBookingsState.Success(emptyList()), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(99, notifications.canceledBookingId)
        assertEquals(1, repository.getBookingsCalls)
    }

    @Test
    fun cancelUserBooking_when_failure_does_not_refresh_or_cancel_notification() = runTest {
        // Arrange
        val repository = FakeUserBookingsRepository(failCancelWithMessage = "fallo")
        val notifications = FakeNotificationManager()
        val viewModel = buildViewModel(repository, notifications)

        // Act
        viewModel.cancelUserBooking(bookingId = 99, currentUser = sampleUser())

        // Assert
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
