package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.remote.implementation.StripeRemoteDataSourceImpl
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

class StripeRemoteDataSourceImplTest {

    @Test
    fun getPublishableKey_happyPath_trims_and_parses() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond(fixtureJson("stripe", "publishable_key_success.json"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = StripeRemoteDataSourceImpl(provider).getPublishableKey()
        assertTrue(result.isSuccess)
        assertEquals("pk_test_123", result.getOrNull())
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("https://api.test/stripe/publishable-key", request.url.toString())
    }

    @Test
    fun createOrGetCustomer_returns_failure_on_http_error() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(fixtureJson("stripe", "error_payload_standard.json"), HttpStatusCode.InternalServerError, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = StripeRemoteDataSourceImpl(provider).createOrGetCustomer()
        assertTrue(result.isFailure)
    }

    @Test
    fun createEphemeralKey_returns_failure_on_malformed_json() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(fixtureJson("stripe", "ephemeral_key_malformed.json"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = StripeRemoteDataSourceImpl(provider).createEphemeralKey("cus_1")
        assertTrue(result.isFailure)
    }

    @Test
    fun createOrGetCustomer_allows_optional_fields_missing() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(fixtureJson("stripe", "customer_optional_missing_success.json"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = StripeRemoteDataSourceImpl(provider).createOrGetCustomer()
        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull()!!.success)
        assertEquals(null, result.getOrNull()!!.data)
    }

    @Test
    fun createSubscription_omits_blank_coupon_code_from_body() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond(fixtureJson("stripe", "subscription_success.json"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = StripeRemoteDataSourceImpl(provider).createSubscription("price_1", 5, 3, "")
        assertTrue(result.isSuccess)
        assertEquals("https://api.test/stripe/subscription", request.url.toString())
        assertEquals(HttpMethod.Post, request.method)
        assertTrue(request.bodyAsText().contains("\"priceId\":\"price_1\""))
        assertTrue(!request.bodyAsText().contains("couponCode"))
    }
}
