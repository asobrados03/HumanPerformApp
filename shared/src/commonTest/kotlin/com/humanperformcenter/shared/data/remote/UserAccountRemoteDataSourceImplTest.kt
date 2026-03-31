package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.remote.implementation.UserAccountRemoteDataSourceImpl
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

class UserAccountRemoteDataSourceImplTest {

    @Test
    fun deleteUser_happyPath_validates_url_method_and_query() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond("", HttpStatusCode.NoContent)
        })

        val result = UserAccountRemoteDataSourceImpl(provider).deleteUser("ana@test.com")

        assertTrue(result.isSuccess)
        assertEquals(HttpMethod.Delete, request.method)
        assertEquals("https://api.test/mobile/user?email=ana%40test.com", request.url.toString())
    }

    @Test
    fun deleteUser_returns_failure_on_http_error() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(fixtureJson("account", "delete_user_error_standard.json"), HttpStatusCode.BadRequest, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserAccountRemoteDataSourceImpl(provider).deleteUser("bad")
        assertTrue(result.isFailure)
    }

    @Test
    fun deleteUser_returns_failure_when_transport_throws_type_error() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            error("invalid response type")
        })
        val result = UserAccountRemoteDataSourceImpl(provider).deleteUser("ana@test.com")
        assertTrue(result.isFailure)
    }

    @Test
    fun deleteUser_supports_empty_email_edge_case() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond("", HttpStatusCode.NoContent)
        })
        val result = UserAccountRemoteDataSourceImpl(provider).deleteUser("")
        assertTrue(result.isSuccess)
        assertEquals("", request.url.parameters["email"])
    }

    @Test
    fun deleteUser_handles_null_like_literal_value() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("", HttpStatusCode.NoContent)
        })
        val result = UserAccountRemoteDataSourceImpl(provider).deleteUser("null")
        assertTrue(result.isSuccess)
    }
}
