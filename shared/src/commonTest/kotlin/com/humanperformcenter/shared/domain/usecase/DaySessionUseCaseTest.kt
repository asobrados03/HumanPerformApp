package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.booking.BookingRequest
import com.humanperformcenter.shared.data.model.booking.DaySession
import com.humanperformcenter.shared.data.model.booking.ReserveResponse
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateResponse
import com.humanperformcenter.shared.domain.repository.DaySessionRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.koin.mp.KoinPlatform.stopKoin
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DaySessionUseCaseTest : KoinTest {

    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun getSessionsByDay_cuandoHaySesiones_devuelveLista() = runTest {
        val expected = listOf(DaySession(1, "2026-03-20", "10:00", 2, "Ana", 3, 10))
        val useCase = buildUseCase(FakeRepo(sessionsResult = Result.success(expected)))
        assertEquals(expected, useCase.getSessionsByDay(1, LocalDate.parse("2026-03-20")).getOrNull())
    }

    @Test
    fun makeBooking_cuandoRequestValido_devuelveReserva() = runTest {
        val req = BookingRequest(1, 2, 3, 4, 5, 6, "2026-04-01T10:00:00")
        val useCase = buildUseCase(FakeRepo(makeBookingResult = Result.success(ReserveResponse("ok", 77))))
        assertEquals(77, useCase.makeBooking(req).getOrNull()?.booking_id)
    }

    @Test
    fun modifyBookingSession_cuandoRequestValido_devuelveMensaje() = runTest {
        val req = ReserveUpdateRequest(1, 2, 3, 4, 5, "2026-04-02")
        val useCase = buildUseCase(FakeRepo(modifyResult = Result.success(ReserveUpdateResponse("updated"))))
        assertEquals("updated", useCase.modifyBookingSession(req).getOrNull()?.message)
    }

    @Test
    fun getTimeslotId_cuandoHoraInvalida_devuelveFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(timeslotResult = Result.failure(IllegalArgumentException("hora inválida"))))
        assertTrue(useCase.getTimeslotId(1, "MONDAY", "99:99").isFailure)
    }

    @Test
    fun fetchServiceIdForProduct_cuandoRepositorioResponde_devuelveIdServicio() = runTest {
        val useCase = buildUseCase(FakeRepo(productServiceResult = Result.success(45)))
        assertEquals(45, useCase.fetchServiceIdForProduct(9).getOrNull())
    }

    @Test
    fun getHolidays_cuandoSinFestivos_devuelveListaVacia() = runTest {
        val useCase = buildUseCase(FakeRepo(holidaysResult = Result.success(emptyList())))
        assertEquals(emptyList(), useCase.getHolidays().getOrNull())
    }

    @Test
    fun makeBooking_cuandoRepositorioFalla_propagaFailure() = runTest {
        val req = BookingRequest(1, 2, 3, 4, 5, 6, "2026-04-01T10:00:00")
        val useCase = buildUseCase(FakeRepo(makeBookingResult = Result.failure(RuntimeException("timeout"))))
        assertTrue(useCase.makeBooking(req).isFailure)
    }

    private fun buildUseCase(repo: DaySessionRepository): DaySessionUseCase {
        startKoin { modules(module { single<DaySessionRepository> { repo }; single { DaySessionUseCase(get()) } }) }
        return KoinPlatform.getKoin().get()
    }

    private class FakeRepo(
        private val sessionsResult: Result<List<DaySession>> = Result.success(emptyList()),
        private val makeBookingResult: Result<ReserveResponse> = Result.success(ReserveResponse("ok", 1)),
        private val modifyResult: Result<ReserveUpdateResponse> = Result.success(ReserveUpdateResponse("ok")),
        private val timeslotResult: Result<Int> = Result.success(10),
        private val productServiceResult: Result<Int> = Result.success(5),
        private val holidaysResult: Result<List<String>> = Result.success(emptyList()),
    ) : DaySessionRepository {
        override suspend fun getSessionsByDay(productId: Int, weekStart: LocalDate) = sessionsResult
        override suspend fun makeBooking(bookingRequest: BookingRequest) = makeBookingResult
        override suspend fun modifyBookingSession(reserveUpdateRequest: ReserveUpdateRequest) = modifyResult
        override suspend fun getUserProductId(customerId: Int): Result<Int> = Result.success(1)
        override suspend fun getProductServiceInfo(productId: Int) = productServiceResult
        override suspend fun getTimeslotId(serviceId: Int, dayOfWeek: String, hour: String) = timeslotResult
        override suspend fun getHolidays() = holidaysResult
    }
}
