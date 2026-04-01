package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.user.UserBooking
import com.humanperformcenter.shared.domain.repository.UserBookingsRepository
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

class UserBookingsUseCaseTest : KoinTest {

    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun getUserBookings_cuandoHayReservas_devuelveLista() = runTest {
        val expected = listOf(UserBooking(10, "2026-03-01", "09:00", "Pilates", "Bono 8"))
        val useCase = buildUseCase(FakeRepo(getResult = Result.success(expected)))
        assertEquals(expected, useCase.getUserBookings(7).getOrNull())
    }

    @Test
    fun getUserBookings_cuandoNoHayReservas_devuelveListaVacia() = runTest {
        val useCase = buildUseCase(FakeRepo(getResult = Result.success(emptyList())))
        assertEquals(emptyList(), useCase.getUserBookings(7).getOrNull())
    }

    @Test
    fun cancelUserBooking_cuandoIdInvalido_devuelveFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(cancelResult = Result.failure(IllegalArgumentException("id inválido"))))
        assertTrue(useCase.cancelUserBooking(-1).isFailure)
    }

    @Test
    fun getUserBookings_cuandoRepositorioFalla_propagaFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(getResult = Result.failure(RuntimeException("timeout"))))
        assertTrue(useCase.getUserBookings(7).isFailure)
    }

    private fun buildUseCase(repo: UserBookingsRepository): UserBookingsUseCase {
        startKoin { modules(module { single<UserBookingsRepository> { repo }; single { UserBookingsUseCase(get()) } }) }
        return KoinPlatform.getKoin().get()
    }

    private class FakeRepo(
        private val getResult: Result<List<UserBooking>> = Result.success(emptyList()),
        private val cancelResult: Result<Unit> = Result.success(Unit),
    ) : UserBookingsRepository {
        override suspend fun getUserBookings(userId: Int) = getResult
        override suspend fun cancelUserBooking(bookingId: Int) = cancelResult
    }
}
