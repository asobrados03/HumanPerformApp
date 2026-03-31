package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.remote.implementation.UserBookingsRemoteDataSourceImpl
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
import kotlin.test.assertTrue

class UserBookingsRemoteDataSourceImplTest {

    @Test
    fun getUserBookings_happyPath_validates_url_query_and_parse() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond(
                """[{"id":1,"date":"2026-03-31","hour":"09:00","service":"PT","product":"Pack","service_id":7,"product_id":8,"coach_name":"Ana","coach_profile_pic":null}]""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        })

        val result = UserBookingsRemoteDataSourceImpl(provider).getUserBookings(12)

        assertTrue(result.isSuccess)
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("https://api.test/mobile/user-bookings?user_id=12", request.url.toString())
        assertEquals("Ana", result.getOrNull()!!.first().coachName)
    }

    @Test
    fun getUserBookings_returns_failure_on_http_error() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("{}", HttpStatusCode.InternalServerError, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserBookingsRemoteDataSourceImpl(provider).getUserBookings(12)
        assertTrue(result.isFailure)
    }

    @Test
    fun getUserBookings_returns_failure_on_wrong_type() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("""[{"id":"bad"}]""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserBookingsRemoteDataSourceImpl(provider).getUserBookings(12)
        assertTrue(result.isFailure)
    }

    @Test
    fun getUserBookings_handles_optional_fields_absent_or_null() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(
                """[{"id":2,"date":"2026-03-31","hour":"10:00","service":"PT","product":"Pack","service_id":null,"product_id":null,"coach_name":"Ana"}]""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        })
        val result = UserBookingsRemoteDataSourceImpl(provider).getUserBookings(1)
        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull()!!.first().coachProfilePic)
    }

    @Test
    fun cancelUserBooking_edge_uses_booking_id_in_path() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond("", HttpStatusCode.NoContent)
        })

        val result = UserBookingsRemoteDataSourceImpl(provider).cancelUserBooking(777)

        assertTrue(result.isSuccess)
        assertEquals(HttpMethod.Delete, request.method)
        assertEquals("https://api.test/mobile/bookings/777", request.url.toString())
    }
}
