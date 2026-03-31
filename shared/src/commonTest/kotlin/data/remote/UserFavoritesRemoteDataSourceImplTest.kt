package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.remote.implementation.UserFavoritesRemoteDataSourceImpl
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

class UserFavoritesRemoteDataSourceImplTest {

    @Test
    fun markFavorite_happyPath_validates_url_method_body_and_parse() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond("""{"message":"saved"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })

        val result = UserFavoritesRemoteDataSourceImpl(provider).markFavorite(3, "Pilates", 7)

        assertTrue(result.isSuccess)
        assertEquals("saved", result.getOrNull())
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("https://api.test/mobile/user/preferred-coach", request.url.toString())
        assertTrue(request.bodyAsText().contains("\"serviceName\":\"Pilates\""))
        assertTrue(request.bodyAsText().contains("\"userId\":7"))
        assertTrue(request.bodyAsText().contains("\"coachId\":3"))
    }

    @Test
    fun getCoaches_returns_failure_on_http_error() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("[]", HttpStatusCode.InternalServerError, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserFavoritesRemoteDataSourceImpl(provider).getCoaches()
        assertTrue(result.isFailure)
    }

    @Test
    fun getCoaches_returns_failure_on_malformed_json() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("{" , HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserFavoritesRemoteDataSourceImpl(provider).getCoaches()
        assertTrue(result.isFailure)
    }

    @Test
    fun getCoaches_handles_optional_fields_absent() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("""[{"id":1,"name":"Ana"}]""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserFavoritesRemoteDataSourceImpl(provider).getCoaches()
        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull()!!.first().photoName)
    }

    @Test
    fun markFavorite_edge_null_inputs_are_normalized() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond("""{"message":"ok"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })

        val result = UserFavoritesRemoteDataSourceImpl(provider).markFavorite(4, null, null)

        assertTrue(result.isSuccess)
        val body = request.bodyAsText()
        assertTrue(body.contains("\"serviceName\":\"\""))
        assertTrue(body.contains("\"userId\":0"))
    }
}
