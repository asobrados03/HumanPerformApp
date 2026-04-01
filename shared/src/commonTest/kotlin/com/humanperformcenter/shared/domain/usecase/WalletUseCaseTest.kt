package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.payment.EwalletTransaction
import com.humanperformcenter.shared.domain.repository.UserWalletRepository
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.koin.mp.KoinPlatform.stopKoin
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WalletUseCaseTest : KoinTest {

    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun getEwalletBalance_cuandoHaySaldo_devuelveBalance() = runTest {
        val useCase = buildUseCase(FakeRepo(balanceResult = Result.success(42.5)))
        assertEquals(42.5, useCase.getEwalletBalance(1).getOrNull())
    }

    @Test
    fun getEwalletBalance_cuandoSaldoNulo_devuelveNull() = runTest {
        val useCase = buildUseCase(FakeRepo(balanceResult = Result.success(null)))
        assertEquals(null, useCase.getEwalletBalance(1).getOrNull())
    }

    @Test
    fun getEwalletTransactions_cuandoNoHayMovimientos_devuelveListaVacia() = runTest {
        val useCase = buildUseCase(FakeRepo(transactionsResult = Result.success(emptyList())))
        assertEquals(emptyList(), useCase.getEwalletTransactions(1).getOrNull())
    }

    @Test
    fun getEwalletTransactions_cuandoRepositorioFalla_propagaFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(transactionsResult = Result.failure(RuntimeException("db"))))
        assertTrue(useCase.getEwalletTransactions(1).isFailure)
    }

    private fun buildUseCase(repo: UserWalletRepository): WalletUseCase {
        startKoin { modules(module { single<UserWalletRepository> { repo }; single { WalletUseCase(get()) } }) }
        return KoinPlatform.getKoin().get()
    }

    private class FakeRepo(
        private val balanceResult: Result<Double?> = Result.success(0.0),
        private val transactionsResult: Result<List<EwalletTransaction>> = Result.success(emptyList()),
    ) : UserWalletRepository {
        override suspend fun getEwalletBalance(userId: Int) = balanceResult
        override suspend fun getEwalletTransactions(userId: Int) = transactionsResult
    }
}
