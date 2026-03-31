package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.remote.implementation.UserStatsRemoteDataSourceImpl
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

class UserStatsRemoteDataSourceImplTest {

    @Test
    fun getUserStats_happyPath_validates_url_method_and_parsing() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond("""{"last_month_workouts":12,"most_frequent_trainer":"Ana","pending_bookings":2}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })

        val result = UserStatsRemoteDataSourceImpl(provider).getUserStats(11)

        assertTrue(result.isSuccess)
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("https://api.test/mobile/users/11/stats", request.url.toString())
        assertEquals(12, result.getOrNull()!!.lastMonthWorkouts)
    }

    @Test
    fun getUserStats_returns_failure_on_http_error() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("{}", HttpStatusCode.NotFound, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserStatsRemoteDataSourceImpl(provider).getUserStats(1)
        assertTrue(result.isFailure)
    }

    @Test
    fun getUserStats_returns_failure_on_wrong_json_type() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("""{"last_month_workouts":"many"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserStatsRemoteDataSourceImpl(provider).getUserStats(1)
        assertTrue(result.isFailure)
    }

    @Test
    fun getUserStats_handles_optional_fields_missing_or_null() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("""{"last_month_workouts":3,"pending_bookings":1}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserStatsRemoteDataSourceImpl(provider).getUserStats(1)
        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull()!!.mostFrequentTrainer)
    }

    @Test
    fun getUserStats_edge_uses_default_values_when_numbers_absent() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserStatsRemoteDataSourceImpl(provider).getUserStats(1)
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.lastMonthWorkouts)
    }
}
