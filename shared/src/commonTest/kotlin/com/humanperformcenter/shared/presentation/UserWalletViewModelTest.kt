package com.humanperformcenter.shared.presentation

import app.cash.turbine.test
import com.humanperformcenter.shared.data.model.payment.EwalletTransaction
import com.humanperformcenter.shared.domain.repository.UserWalletRepository
import com.humanperformcenter.shared.domain.usecase.WalletUseCase
import com.humanperformcenter.shared.presentation.ui.EwalletUiState
import com.humanperformcenter.shared.presentation.ui.WalletBalanceUiState
import com.humanperformcenter.shared.presentation.viewmodel.UserWalletViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserWalletViewModelTest {

    private class FakeWalletRepository(
        initialBalances: Map<Int, Double?> = mapOf(1 to 10.0),
        initialTransactions: Map<Int, List<EwalletTransaction>> = emptyMap(),
        private val failBalanceWithMessage: String? = null,
        private val failTransactionsWithMessage: String? = null,
        private val failTransactionsWithoutMessage: Boolean = false
    ) : UserWalletRepository {
        private val balances = initialBalances.toMutableMap()
        private val transactions = initialTransactions.toMutableMap()

        override suspend fun getEwalletBalance(userId: Int): Result<Double?> {
            failBalanceWithMessage?.let { return Result.failure(IllegalStateException(it)) }
            return Result.success(balances[userId])
        }

        override suspend fun getEwalletTransactions(userId: Int): Result<List<EwalletTransaction>> {
            if (failTransactionsWithoutMessage) return Result.failure(IllegalStateException())
            failTransactionsWithMessage?.let { return Result.failure(IllegalStateException(it)) }
            return Result.success(transactions[userId].orEmpty())
        }
    }

    private fun buildViewModel(
        repository: UserWalletRepository = FakeWalletRepository()
    ) = UserWalletViewModel(WalletUseCase(repository))

    @Test
    fun loadBalance_when_success_updates_balance() = runTest {
        // Arrange
        val viewModel = buildViewModel(
        // Act
            FakeWalletRepository(initialBalances = mapOf(1 to 42.5))
        )

        viewModel.walletBalanceUiState.test {
        // Assert
            assertEquals(WalletBalanceUiState.Idle, awaitItem())
            viewModel.loadBalance(1)
            assertEquals(WalletBalanceUiState.Loading, awaitItem())
            assertEquals(WalletBalanceUiState.Success(42.5), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadBalance_when_repository_returns_null_sets_zero() = runTest {
        // Arrange
        val viewModel = buildViewModel(
        // Act
            FakeWalletRepository(initialBalances = mapOf(1 to null))
        )

        viewModel.walletBalanceUiState.test {
        // Assert
            assertEquals(WalletBalanceUiState.Idle, awaitItem())
            viewModel.loadBalance(1)
            assertEquals(WalletBalanceUiState.Loading, awaitItem())
            assertEquals(WalletBalanceUiState.Success(0.0), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadBalance_when_user_id_is_invalid_keeps_zero() = runTest {
        // Arrange
        val viewModel = buildViewModel(FakeWalletRepository(initialBalances = mapOf(1 to 99.9)))

        // Act
        viewModel.walletBalanceUiState.test {
        // Assert
            assertEquals(WalletBalanceUiState.Idle, awaitItem())
            viewModel.loadBalance(-1)
            assertEquals(WalletBalanceUiState.Error("Usuario inválido"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadBalance_when_repository_fails_keeps_last_balance() = runTest {
        // Arrange
        val viewModel = buildViewModel(
        // Act
            FakeWalletRepository(failBalanceWithMessage = "Sin conexión")
        )

        viewModel.walletBalanceUiState.test {
        // Assert
            assertEquals(WalletBalanceUiState.Idle, awaitItem())
            viewModel.loadBalance(1)
            assertEquals(WalletBalanceUiState.Loading, awaitItem())
            assertEquals(WalletBalanceUiState.Error("Sin conexión"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadEWalletTransactions_when_success_emits_loading_then_success() = runTest {
        // Arrange
        val tx = EwalletTransaction(5.0, 10.0, "Recarga", "credit", "2026-03-01")
        val viewModel = buildViewModel(
        // Act
            FakeWalletRepository(initialTransactions = mapOf(1 to listOf(tx)))
        )

        viewModel.eWalletTransactions.test {
        // Assert
            assertEquals(EwalletUiState.Loading, awaitItem())
            viewModel.loadEwalletTransactions(1)
            assertEquals(EwalletUiState.Success(listOf(tx)), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadEWalletTransactions_when_failure_with_message_emits_error() = runTest {
        // Arrange
        val viewModel = buildViewModel(
        // Act
            FakeWalletRepository(failTransactionsWithMessage = "Sin conexión")
        )

        viewModel.eWalletTransactions.test {
        // Assert
            assertEquals(EwalletUiState.Loading, awaitItem())
            viewModel.loadEwalletTransactions(1)
            assertEquals(EwalletUiState.Error("Sin conexión"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadEWalletTransactions_when_failure_without_message_emits_unknown_error() = runTest {
        // Arrange
        val viewModel = buildViewModel(
        // Act
            FakeWalletRepository(failTransactionsWithoutMessage = true)
        )

        viewModel.eWalletTransactions.test {
        // Assert
            assertEquals(EwalletUiState.Loading, awaitItem())
            viewModel.loadEwalletTransactions(1)
            assertEquals(EwalletUiState.Error("Error desconocido"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
