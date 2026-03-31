package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.remote.implementation.ServiceProductRemoteDataSourceImpl
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

class ServiceProductRemoteDataSourceImplTest {

    @Test
    fun assignProductToUser_happyPath_validates_url_method_body_and_parse() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond("""{"assignedId":99}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })

        val result = ServiceProductRemoteDataSourceImpl(provider)
            .assignProductToUser(7, 21, "card", "SAVE10")

        assertTrue(result.isSuccess)
        assertEquals(99, result.getOrNull())
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("https://api.test/mobile/users/7/products", request.url.toString())
        assertEquals(ContentType.Application.Json.toString(), request.requestContentType())
        val body = request.bodyAsText()
        assertTrue(body.contains("\"productId\":21"))
        assertTrue(body.contains("\"paymentMethod\":\"card\""))
        assertTrue(body.contains("\"couponCode\":\"SAVE10\""))
    }

    @Test
    fun assignProductToUser_returns_failure_on_http_error() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("{}", HttpStatusCode.BadRequest, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = ServiceProductRemoteDataSourceImpl(provider).assignProductToUser(1, 1, "card", null)
        assertTrue(result.isFailure)
    }

    @Test
    fun getUserProducts_returns_failure_on_wrong_type_json() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("""[{"id":"bad"}]""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = ServiceProductRemoteDataSourceImpl(provider).getUserProducts(3)
        assertTrue(result.isFailure)
    }

    @Test
    fun assignProductToUser_handles_optional_coupon_absent() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond("""{"assignedId":1}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = ServiceProductRemoteDataSourceImpl(provider).assignProductToUser(1, 4, "cash", null)
        assertTrue(result.isSuccess)
        assertTrue(request.bodyAsText().contains("\"couponCode\":null"))
    }

    @Test
    fun assignProductToUser_edge_missing_assigned_id_defaults_to_zero() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = ServiceProductRemoteDataSourceImpl(provider).assignProductToUser(1, 1, "cash", null)
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }
}
