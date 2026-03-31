package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.remote.implementation.DaySessionRemoteDataSourceImpl
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DaySessionRemoteDataSourceImplTest {

    @Test
    fun getSessionsByDay_happyPath_builds_expected_request_and_parses() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond(
                """[{"product_id":10,"date":"2026-03-31","hour":"10:00","coach_id":2,"coach_name":"Ana","booked":1,"capacity":8}]""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        })

        val result = DaySessionRemoteDataSourceImpl(provider)
            .getSessionsByDay(10, kotlinx.datetime.LocalDate.parse("2026-03-31"))

        assertTrue(result.isSuccess)
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("https://api.test/mobile/daily?product_id=10&date=2026-03-31", request.url.toString())
        assertEquals("Ana", result.getOrNull()!!.first().coachName)
    }

    @Test
    fun getSessionsByDay_returns_failure_on_http_error() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("{}", HttpStatusCode.InternalServerError, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = DaySessionRemoteDataSourceImpl(provider)
            .getSessionsByDay(1, kotlinx.datetime.LocalDate.parse("2026-03-31"))
        assertTrue(result.isFailure)
    }

    @Test
    fun getSessionsByDay_returns_failure_on_malformed_json() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("{" , HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = DaySessionRemoteDataSourceImpl(provider)
            .getSessionsByDay(1, kotlinx.datetime.LocalDate.parse("2026-03-31"))
        assertTrue(result.isFailure)
    }

    @Test
    fun getSessionsByDay_handles_optional_field_absent() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(
                """[{"product_id":10,"date":"2026-03-31","hour":"10:00","coach_id":2,"booked":1,"capacity":8}]""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        })
        val result = DaySessionRemoteDataSourceImpl(provider)
            .getSessionsByDay(10, kotlinx.datetime.LocalDate.parse("2026-03-31"))
        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull()!!.first().coachName)
    }

    @Test
    fun getTimeslotId_normalizes_hour_and_fails_when_session_id_missing() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond("""{"other":4}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })

        val result = DaySessionRemoteDataSourceImpl(provider).getTimeslotId(8, "MONDAY", "09:00")

        assertEquals("09:00:00", request.url.parameters["hour"])
        assertTrue(result.isFailure)
        assertIs<IllegalStateException>(result.exceptionOrNull())
    }
}
