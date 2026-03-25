package com.humanperformcenter.shared.domain.usecases

import com.humanperformcenter.shared.data.model.user.Professional
import com.humanperformcenter.shared.domain.repository.UserFavoritesRepository
import com.humanperformcenter.shared.domain.usecase.UserCoachesUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserCoachesUseCaseTest {
    private class FakeUserFavoritesRepository(
        private val coachesResult: Result<List<Professional>>
    ) : UserFavoritesRepository {
        override suspend fun getCoaches(): Result<List<Professional>> = coachesResult
        override suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?) = error("Not used")
        override suspend fun getPreferredCoach(customerId: Int) = error("Not used")
    }

    @Test
    fun userCoachesUseCase_whenRepositoryReturnsCoaches_returnsSuccess() = runBlocking {
        // Arrange
        val coaches = listOf(Professional(id = 1, name = "Coach A"))
        val useCase = UserCoachesUseCase(FakeUserFavoritesRepository(Result.success(coaches)))

        // Act
        val result = useCase.getCoaches()

        // Assert
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun userCoachesUseCase_whenRepositoryReturnsEmptyList_returnsEmptyList() = runBlocking {
        // Arrange
        val useCase = UserCoachesUseCase(FakeUserFavoritesRepository(Result.success(emptyList())))

        // Act
        val result = useCase.getCoaches()

        // Assert
        assertTrue(result.getOrNull().isNullOrEmpty())
    }

    @Test
    fun userCoachesUseCase_whenRepositoryFails_propagatesFailure() = runBlocking {
        // Arrange
        val useCase = UserCoachesUseCase(FakeUserFavoritesRepository(Result.failure(RuntimeException("network"))))

        // Act
        val result = useCase.getCoaches()

        // Assert
        assertTrue(result.isFailure)
    }
}
