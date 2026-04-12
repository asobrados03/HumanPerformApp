package com.humanperformcenter.shared.integration

import com.humanperformcenter.shared.data.model.booking.BookingRequest
import com.humanperformcenter.shared.data.model.booking.ReserveUpdateRequest
import com.humanperformcenter.shared.data.persistence.DaySessionRepositoryImpl
import com.humanperformcenter.shared.data.remote.implementation.DaySessionRemoteDataSourceImpl
import com.humanperformcenter.shared.domain.DomainException
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase
import integration.integrationProvider
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BookingFlowIntegrationTest {

    @Test
    fun daily_sessions_reserve_modify_and_holidays_flow_is_consistent() = runTest {
        val apiEngine = MockEngine { request ->
            when (request.method) {
                HttpMethod.Get if request.url.encodedPath == "/mobile/daily" -> respond(
                    """[{"product_id":31,"date":"2026-03-31","hour":"10:00","coach_id":3,"coach_name":"Paula","booked":2,"capacity":8}]""",
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
                HttpMethod.Post if request.url.encodedPath == "/mobile/bookings" -> respond(
                    """{"message":"reserved","booking_id":701}""",
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
                HttpMethod.Patch if request.url.encodedPath == "/mobile/bookings/701" -> respond(
                    """{"message":"updated"}""",
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
                HttpMethod.Get if request.url.encodedPath == "/mobile/holidays" -> respond(
                    """["2026-04-03","2026-05-01"]""",
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
                else -> error("Unhandled api endpoint: ${request.method} ${request.url}")
            }
        }

        val useCase = DaySessionUseCase(
            DaySessionRepositoryImpl(DaySessionRemoteDataSourceImpl(integrationProvider(apiEngine = apiEngine))),
        )

        val daily = useCase.getSessionsByDay(productId = 31, date = LocalDate.parse("2026-03-31"))
        val reserve = useCase.makeBooking(
            BookingRequest(
                customerId = 22,
                coachId = 3,
                sessionTimeslotId = 40,
                serviceId = 1,
                productId = 31,
                centerId = 1,
                startDate = "2026-03-31T10:00:00",
            ),
        )
        val modify = useCase.modifyBookingSession(
            ReserveUpdateRequest(
                booking_id = 701,
                new_coach_id = 4,
                new_service_id = 1,
                new_product_id = 31,
                new_session_timeslot_id = 41,
                new_start_date = "2026-04-01",
            ),
        )
        val holidays = useCase.getHolidays()

        assertTrue(daily.isSuccess)
        assertEquals("Paula", daily.getOrThrow().first().coachName)
        assertTrue(reserve.isSuccess)
        assertEquals(701, reserve.getOrThrow().booking_id)
        assertTrue(modify.isSuccess)
        assertEquals("updated", modify.getOrThrow().message)
        assertTrue(holidays.isSuccess)
        assertEquals(listOf("2026-04-03", "2026-05-01"), holidays.getOrThrow())
    }

    @Test
    fun get_sessions_by_day_with_server_error_maps_to_domain_server_error() = runTest {
        val apiEngine = MockEngine { request ->
            when (request.method) {
                HttpMethod.Get if request.url.encodedPath == "/mobile/daily" -> respond(
                    """{"error":"down"}""",
                    HttpStatusCode.InternalServerError,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
                else -> error("Unhandled api endpoint: ${request.method} ${request.url}")
            }
        }

        val useCase = DaySessionUseCase(
            DaySessionRepositoryImpl(DaySessionRemoteDataSourceImpl(integrationProvider(apiEngine = apiEngine))),
        )

        val result = useCase.getSessionsByDay(productId = 31, date = LocalDate.parse("2026-03-31"))

        assertTrue(result.isFailure)
        assertIs<DomainException.Server>(result.exceptionOrNull())
    }

    @Test
    fun get_holidays_with_malformed_payload_maps_to_parsing_error() = runTest {
        val apiEngine = MockEngine { request ->
            when (request.method) {
                HttpMethod.Get if request.url.encodedPath == "/mobile/holidays" -> respond(
                    """{"holidays":"not-an-array"}""",
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
                else -> error("Unhandled api endpoint: ${request.method} ${request.url}")
            }
        }

        val useCase = DaySessionUseCase(
            DaySessionRepositoryImpl(DaySessionRemoteDataSourceImpl(integrationProvider(apiEngine = apiEngine))),
        )

        val result = useCase.getHolidays()

        assertTrue(result.isFailure)
        assertIs<DomainException.Parsing>(result.exceptionOrNull())
    }
}
