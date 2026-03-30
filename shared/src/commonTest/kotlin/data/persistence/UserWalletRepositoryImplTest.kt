package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.payment.EwalletTransaction
import com.humanperformcenter.shared.data.remote.UserWalletRemoteDataSource
import com.humanperformcenter.shared.domain.DomainException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UserWalletRepositoryImplTest {

    @Test
    fun getewalletbalance_when_success_returns_balance() = runTest {
        val remote = FakeUserWalletRemoteDataSource(balanceResult = Result.success(150.5))
        val repository = UserWalletRepositoryImpl(remote)

        val result = repository.getEwalletBalance(userId = 7)

        assertTrue(result.isSuccess)
        assertEquals(150.5, result.getOrNull())
    }

    @Test
    fun getewallettransactions_when_backend_error_maps_to_unauthorized() = runTest {
        val remote = FakeUserWalletRemoteDataSource(
            transactionsResult = Result.failure(IllegalStateException("HTTP 401 Unauthorized")),
        )
        val repository = UserWalletRepositoryImpl(remote)

        val result = repository.getEwalletTransactions(userId = 7)

        assertTrue(result.isFailure)
        assertIs<DomainException.Unauthorized>(result.exceptionOrNull())
    }

    @Test
    fun getewallettransactions_when_network_exception_maps_to_domain_network() = runTest {
        val remote = FakeUserWalletRemoteDataSource(
            transactionsResult = Result.failure(IOException("Connection error")),
        )
        val repository = UserWalletRepositoryImpl(remote)

        val result = repository.getEwalletTransactions(userId = 7)

        assertTrue(result.isFailure)
        assertIs<DomainException.Network>(result.exceptionOrNull())
    }

    private class FakeUserWalletRemoteDataSource(
        private val balanceResult: Result<Double?> = Result.success(0.0),
        private val transactionsResult: Result<List<EwalletTransaction>> = Result.success(
            listOf(
                EwalletTransaction(
                    amount = 30.0,
                    balance = 100.0,
                    description = "Ingreso",
                    type = "credit",
                    date = "2026-03-30",
                ),
            ),
        ),
    ) : UserWalletRemoteDataSource {
        override suspend fun getEwalletBalance(userId: Int): Result<Double?> = balanceResult

        override suspend fun getEwalletTransactions(userId: Int): Result<List<EwalletTransaction>> = transactionsResult
    }
}
