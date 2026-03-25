package com.humanperformcenter.shared.domain.usecases

import com.humanperformcenter.shared.domain.repository.DaySessionRepository
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DaySessionUseCaseTest {
    private class FakeDaySessionRepository(
        private val holidaysResult: Result<List<String>>
    ) : DaySessionRepository {
        override suspend fun getSessionsByDay(productId: Int, weekStart: LocalDate) = error("Not used")
        override suspend fun makeBooking(bookingRequest: com.humanperformcenter.shared.data.model.booking.BookingRequest) = error("Not used")
        override suspend fun modifyBookingSession(reserveUpdateRequest: com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest) = error("Not used")
        override suspend fun getUserProductId(customerId: Int) = error("Not used")
        override suspend fun getProductServiceInfo(productId: Int) = error("Not used")
        override suspend fun getTimeslotId(serviceId: Int, dayOfWeek: String, hour: String) = error("Not used")
        override suspend fun getHolidays(): Result<List<String>> = holidaysResult
    }

    @Test
    fun daySessionUseCase_whenRepositoryReturnsHolidays_returnsSuccess() = runBlocking {
        // Arrange
        val expected = listOf("2026-01-06")
        val useCase = DaySessionUseCase(FakeDaySessionRepository(Result.success(expected)))

        // Act
        val result = useCase.getHolidays()

        // Assert
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun daySessionUseCase_whenRepositoryReturnsEmptyHolidays_returnsEmptyList() = runBlocking {
        // Arrange
        val useCase = DaySessionUseCase(FakeDaySessionRepository(Result.success(emptyList())))

        // Act
        val result = useCase.getHolidays()

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull().isNullOrEmpty())
    }

    @Test
    fun daySessionUseCase_whenRepositoryFails_propagatesFailure() = runBlocking {
        // Arrange
        val useCase = DaySessionUseCase(FakeDaySessionRepository(Result.failure(IllegalStateException("boom"))))

        // Act
        val result = useCase.getHolidays()

        // Assert
        assertTrue(result.isFailure)
    }
}
