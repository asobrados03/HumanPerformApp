package com.humanperformcenter.shared.domain.usecases

import com.humanperformcenter.shared.domain.repository.UserWalletRepository
import com.humanperformcenter.shared.domain.usecase.WalletUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WalletUseCaseTest {
    private class FakeWalletRepository(
        private val balanceResult: Result<Double?>
    ) : UserWalletRepository {
        override suspend fun getEwalletBalance(userId: Int): Result<Double?> = balanceResult
        override suspend fun getEwalletTransactions(userId: Int) = error("Not used")
    }

    @Test
    fun walletUseCase_whenRepositoryReturnsBalance_returnsSuccess() = runBlocking {
        // Arrange
        val useCase = WalletUseCase(FakeWalletRepository(Result.success(45.0)))

        // Act
        val result = useCase.getEwalletBalance(1)

        // Assert
        assertEquals(45.0, result.getOrNull())
    }

    @Test
    fun walletUseCase_whenRepositoryReturnsNullBalance_returnsNull() = runBlocking {
        // Arrange
        val useCase = WalletUseCase(FakeWalletRepository(Result.success(null)))

        // Act
        val result = useCase.getEwalletBalance(1)

        // Assert
        assertEquals(null, result.getOrNull())
    }

    @Test
    fun walletUseCase_whenRepositoryFails_propagatesFailure() = runBlocking {
        // Arrange
        val useCase = WalletUseCase(FakeWalletRepository(Result.failure(IllegalStateException("boom"))))

        // Act
        val result = useCase.getEwalletBalance(1)

        // Assert
        assertTrue(result.isFailure)
    }
}
