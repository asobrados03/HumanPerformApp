package com.humanperformcenter.shared.domain.usecases

import com.humanperformcenter.shared.data.model.user.UserStatistics
import com.humanperformcenter.shared.domain.repository.UserStatsRepository
import com.humanperformcenter.shared.domain.usecase.UserStatsUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserStatsUseCaseTest {
    private class FakeUserStatsRepository(
        private val statsResult: Result<UserStatistics>
    ) : UserStatsRepository {
        override suspend fun getUserStats(customerId: Int): Result<UserStatistics> = statsResult
    }

    @Test
    fun userStatsUseCase_whenRepositoryReturnsStats_returnsSuccess() = runBlocking {
        // Arrange
        val stats = UserStatistics(lastMonthWorkouts = 8, mostFrequentTrainer = "Ana", pendingBookings = 2)
        val useCase = UserStatsUseCase(FakeUserStatsRepository(Result.success(stats)))

        // Act
        val result = useCase.getUserStats(1)

        // Assert
        assertEquals(8, result.getOrNull()?.lastMonthWorkouts)
    }

    @Test
    fun userStatsUseCase_whenRepositoryReturnsZeroStats_returnsZeros() = runBlocking {
        // Arrange
        val stats = UserStatistics()
        val useCase = UserStatsUseCase(FakeUserStatsRepository(Result.success(stats)))

        // Act
        val result = useCase.getUserStats(1)

        // Assert
        assertEquals(0, result.getOrNull()?.lastMonthWorkouts)
    }

    @Test
    fun userStatsUseCase_whenRepositoryFails_propagatesFailure() = runBlocking {
        // Arrange
        val useCase = UserStatsUseCase(FakeUserStatsRepository(Result.failure(RuntimeException("network"))))

        // Act
        val result = useCase.getUserStats(1)

        // Assert
        assertTrue(result.isFailure)
    }
}
