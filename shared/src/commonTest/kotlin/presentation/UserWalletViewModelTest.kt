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
        private val transactionsResult: Result<List<EwalletTransaction>>
    ) : UserWalletRepository {
        override suspend fun getEwalletBalance(userId: Int): Result<Double?> = Result.success(10.0)
        override suspend fun getEwalletTransactions(userId: Int): Result<List<EwalletTransaction>> = transactionsResult
    }

    @Test
    fun userWalletViewModel_whenLoadTransactionsSucceeds_emitsLoadingThenSuccess() = runTest {
        // Arrange
        val tx = EwalletTransaction(5.0, 10.0, "Recarga", "credit", "2026-03-01")
        val viewModel = UserWalletViewModel(WalletUseCase(FakeWalletRepository(Result.success(listOf(tx)))))

        // Act + Assert
        viewModel.eWalletTransactions.test {
            assertEquals(EwalletUiState.Loading, awaitItem())
            viewModel.loadEwalletTransactions(1)
            assertEquals(EwalletUiState.Loading, awaitItem())
            assertEquals(EwalletUiState.Success(listOf(tx)), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun userWalletViewModel_whenLoadTransactionsFails_emitsLoadingThenError() = runTest {
        // Arrange
        val viewModel = UserWalletViewModel(
            WalletUseCase(FakeWalletRepository(Result.failure(IllegalStateException("Sin conexión"))))
        )

        // Act + Assert
        viewModel.eWalletTransactions.test {
            assertEquals(EwalletUiState.Loading, awaitItem())
            viewModel.loadEwalletTransactions(1)
            assertEquals(EwalletUiState.Loading, awaitItem())
            assertEquals(EwalletUiState.Error("Sin conexión"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
