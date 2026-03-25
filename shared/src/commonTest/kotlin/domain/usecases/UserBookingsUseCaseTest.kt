package com.humanperformcenter.shared.domain.usecases

import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.domain.repository.UserBookingsRepository
import com.humanperformcenter.shared.domain.usecase.UserBookingsUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserBookingsUseCaseTest {
    private class FakeUserBookingsRepository(
        private val getBookingsResult: Result<List<UserBooking>>
    ) : UserBookingsRepository {
        override suspend fun getUserBookings(userId: Int): Result<List<UserBooking>> = getBookingsResult
        override suspend fun cancelUserBooking(bookingId: Int): Result<Unit> = Result.success(Unit)
    }

    @Test
    fun userBookingsUseCase_whenRepositoryReturnsBookings_returnsSuccess() = runBlocking {
        // Arrange
        val bookings = listOf(UserBooking(1, "2026-03-10", "09:00", "Fisio", "Pack", 1, 1, "Ana", null))
        val useCase = UserBookingsUseCase(FakeUserBookingsRepository(Result.success(bookings)))

        // Act
        val result = useCase.getUserBookings(10)

        // Assert
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun userBookingsUseCase_whenRepositoryReturnsEmptyList_returnsEmptyList() = runBlocking {
        // Arrange
        val useCase = UserBookingsUseCase(FakeUserBookingsRepository(Result.success(emptyList())))

        // Act
        val result = useCase.getUserBookings(10)

        // Assert
        assertTrue(result.getOrNull().isNullOrEmpty())
    }

    @Test
    fun userBookingsUseCase_whenRepositoryFails_propagatesFailure() = runBlocking {
        // Arrange
        val useCase = UserBookingsUseCase(FakeUserBookingsRepository(Result.failure(IllegalStateException("boom"))))

        // Act
        val result = useCase.getUserBookings(10)

        // Assert
        assertTrue(result.isFailure)
    }
}
