package com.humanperformcenter.shared.presentation

import app.cash.turbine.test
import com.humanperformcenter.shared.data.model.payment.EwalletTransaction
import com.humanperformcenter.shared.domain.repository.UserWalletRepository
import com.humanperformcenter.shared.domain.usecase.WalletUseCase
import com.humanperformcenter.shared.presentation.ui.EwalletUiState
import com.humanperformcenter.shared.presentation.viewmodel.UserWalletViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserWalletViewModelTest {

    private class FakeWalletRepository(
        private val balanceResult: Result<Double?> = Result.success(10.0),
        private val transactionsResult: Result<List<EwalletTransaction>> = Result.success(emptyList())
    ) : UserWalletRepository {
        override suspend fun getEwalletBalance(userId: Int): Result<Double?> = balanceResult
        override suspend fun getEwalletTransactions(userId: Int): Result<List<EwalletTransaction>> = transactionsResult
    }

    private fun buildViewModel(
        balanceResult: Result<Double?> = Result.success(10.0),
        transactionsResult: Result<List<EwalletTransaction>> = Result.success(emptyList())
    ) = UserWalletViewModel(WalletUseCase(FakeWalletRepository(balanceResult, transactionsResult)))

    @Test
    fun loadbalance_when_success_updates_balance() = runTest {
        val viewModel = buildViewModel(balanceResult = Result.success(42.5))

        viewModel.balance.test {
            assertEquals(0.0, awaitItem())
            viewModel.loadBalance(1)
            assertEquals(42.5, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadbalance_when_repository_returns_null_sets_zero() = runTest {
        val viewModel = buildViewModel(balanceResult = Result.success(null))

        viewModel.balance.test {
            assertEquals(0.0, awaitItem())
            viewModel.loadBalance(1)
            assertEquals(0.0, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadbalance_when_user_id_is_invalid_keeps_zero() = runTest {
        val viewModel = buildViewModel(balanceResult = Result.success(99.9))

        viewModel.balance.test {
            assertEquals(0.0, awaitItem())
            viewModel.loadBalance(-1)
            assertEquals(0.0, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadbalance_when_repository_fails_keeps_last_balance() = runTest {
        val viewModel = buildViewModel(balanceResult = Result.failure(IllegalStateException("Sin conexión")))

        viewModel.balance.test {
            assertEquals(0.0, awaitItem())
            viewModel.loadBalance(1)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadewallettransactions_when_success_emits_loading_then_success() = runTest {
        val tx = EwalletTransaction(5.0, 10.0, "Recarga", "credit", "2026-03-01")
        val viewModel = buildViewModel(transactionsResult = Result.success(listOf(tx)))

        viewModel.eWalletTransactions.test {
            assertEquals(EwalletUiState.Loading, awaitItem())
            viewModel.loadEwalletTransactions(1)
            assertEquals(EwalletUiState.Loading, awaitItem())
            assertEquals(EwalletUiState.Success(listOf(tx)), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadewallettransactions_when_failure_with_message_emits_error() = runTest {
        val viewModel = buildViewModel(
            transactionsResult = Result.failure(IllegalStateException("Sin conexión"))
        )

        viewModel.eWalletTransactions.test {
            assertEquals(EwalletUiState.Loading, awaitItem())
            viewModel.loadEwalletTransactions(1)
            assertEquals(EwalletUiState.Loading, awaitItem())
            assertEquals(EwalletUiState.Error("Sin conexión"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadewallettransactions_when_failure_without_message_emits_unknown_error() = runTest {
        val viewModel = buildViewModel(
            transactionsResult = Result.failure(IllegalStateException())
        )

        viewModel.eWalletTransactions.test {
            assertEquals(EwalletUiState.Loading, awaitItem())
            viewModel.loadEwalletTransactions(1)
            assertEquals(EwalletUiState.Loading, awaitItem())
            assertEquals(EwalletUiState.Error("Error desconocido"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
