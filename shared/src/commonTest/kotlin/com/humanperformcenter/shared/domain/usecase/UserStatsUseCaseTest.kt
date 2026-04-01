package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.user.UserStatistics
import com.humanperformcenter.shared.domain.repository.UserStatsRepository
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

class UserStatsUseCaseTest : KoinTest {

    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun getUserStats_cuandoRespuestaValida_devuelveEstadisticas() = runTest {
        val expected = UserStatistics(12, "Laura", 3)
        val useCase = buildUseCase(FakeRepo(Result.success(expected)))
        assertEquals(expected, useCase.getUserStats(1).getOrNull())
    }

    @Test
    fun getUserStats_cuandoSinDatos_devuelveEstadisticasVacias() = runTest {
        val expected = UserStatistics(0, null, 0)
        val useCase = buildUseCase(FakeRepo(Result.success(expected)))
        assertEquals(expected, useCase.getUserStats(1).getOrNull())
    }

    @Test
    fun getUserStats_cuandoCustomerIdInvalido_devuelveFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(Result.failure(IllegalArgumentException("id inválido"))))
        assertTrue(useCase.getUserStats(0).isFailure)
    }

    @Test
    fun getUserStats_cuandoRepositorioFalla_propagaFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(Result.failure(RuntimeException("down"))))
        assertTrue(useCase.getUserStats(1).isFailure)
    }

    private fun buildUseCase(repo: UserStatsRepository): UserStatsUseCase {
        startKoin { modules(module { single<UserStatsRepository> { repo }; single { UserStatsUseCase(get()) } }) }
        return KoinPlatform.getKoin().get()
    }

    private class FakeRepo(private val result: Result<UserStatistics>) : UserStatsRepository {
        override suspend fun getUserStats(customerId: Int) = result
    }
}
