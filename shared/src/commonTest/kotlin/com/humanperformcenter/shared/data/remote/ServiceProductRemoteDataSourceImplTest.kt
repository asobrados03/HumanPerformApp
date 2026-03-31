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
            respond(
                fixtureJson("service", "assign_product_success.json"),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        })

        val result = ServiceProductRemoteDataSourceImpl(provider)
            .assignProductToUser(7, 21, "card", "SAVE10")

        // 👇 importante: si falla sabrás por qué
        assertTrue(result.isSuccess)

        assertEquals(99, result.getOrNull())

        assertEquals(HttpMethod.Post, request.method)
        assertEquals("https://api.test/mobile/users/7/products", request.url.toString())

        // ✔️ más robusto
        assertEquals(request.requestContentType()?.contains("application/json"), true)

        val body = request.bodyAsText()

        // ✔️ menos frágil (permite espacios)
        assertTrue(body.contains("\"product_id\":21"))
        assertTrue(body.contains("\"payment_method\":\"card\""))
        assertTrue(body.contains("\"coupon_code\":\"SAVE10\""))
    }

    @Test
    fun assignProductToUser_returns_failure_on_http_error() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(fixtureJson("service", "assign_product_error_standard.json"), HttpStatusCode.BadRequest, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = ServiceProductRemoteDataSourceImpl(provider).assignProductToUser(1, 1, "card", null)
        assertTrue(result.isFailure)
    }

    @Test
    fun getUserProducts_returns_failure_on_wrong_type_json() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(fixtureJson("service", "user_products_wrong_type.json"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = ServiceProductRemoteDataSourceImpl(provider).getUserProducts(3)
        assertTrue(result.isFailure)
    }

    @Test
    fun assignProductToUser_handles_optional_coupon_absent() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond(fixtureJson("service", "assign_product_optional_nulls_success.json"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = ServiceProductRemoteDataSourceImpl(provider).assignProductToUser(1, 4, "cash", null)
        assertTrue(result.isSuccess)
        assertTrue(request.bodyAsText().contains("\"coupon_code\":null"))
    }

    @Test
    fun assignProductToUser_edge_missing_assigned_id_defaults_to_zero() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(fixtureJson("service", "assign_product_missing_id_success.json"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = ServiceProductRemoteDataSourceImpl(provider).assignProductToUser(1, 1, "cash", null)
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }
}
