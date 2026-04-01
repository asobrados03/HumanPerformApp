package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.user.GetPreferredCoachResponse
import com.humanperformcenter.shared.data.model.user.Professional
import com.humanperformcenter.shared.domain.repository.UserFavoritesRepository
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

class UserCoachesUseCaseTest : KoinTest {

    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun getCoaches_cuandoHayCoaches_devuelveLista() = runTest {
        val expected = listOf(Professional(1, "Coach Ana", "ana.jpg", "Yoga"))
        val useCase = buildUseCase(FakeRepo(getCoachesResult = Result.success(expected)))
        assertEquals(expected, useCase.getCoaches().getOrNull())
    }

    @Test
    fun markFavorite_cuandoServiceNameNulo_devuelveMensaje() = runTest {
        val useCase = buildUseCase(FakeRepo(markFavoriteResult = Result.success("favorite updated")))
        assertEquals("favorite updated", useCase.markFavorite(1, null, 10).getOrNull())
    }

    @Test
    fun getPreferredCoach_cuandoCustomerIdValido_devuelveCoachId() = runTest {
        val useCase = buildUseCase(FakeRepo(preferredResult = Result.success(GetPreferredCoachResponse(7))))
        assertEquals(7, useCase.getPreferredCoach(5).getOrNull()?.coachId)
    }

    @Test
    fun getPreferredCoach_cuandoRepositorioFalla_propagaFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(preferredResult = Result.failure(RuntimeException("db"))))
        assertTrue(useCase.getPreferredCoach(5).isFailure)
    }

    private fun buildUseCase(repo: UserFavoritesRepository): UserCoachesUseCase {
        startKoin { modules(module { single<UserFavoritesRepository> { repo }; single { UserCoachesUseCase(get()) } }) }
        return KoinPlatform.getKoin().get()
    }

    private class FakeRepo(
        private val getCoachesResult: Result<List<Professional>> = Result.success(emptyList()),
        private val markFavoriteResult: Result<String> = Result.success("ok"),
        private val preferredResult: Result<GetPreferredCoachResponse> = Result.success(GetPreferredCoachResponse(1)),
    ) : UserFavoritesRepository {
        override suspend fun getCoaches() = getCoachesResult
        override suspend fun markFavorite(coachId: Int, serviceName: String?, userId: Int?) = markFavoriteResult
        override suspend fun getPreferredCoach(customerId: Int) = preferredResult
    }
}
