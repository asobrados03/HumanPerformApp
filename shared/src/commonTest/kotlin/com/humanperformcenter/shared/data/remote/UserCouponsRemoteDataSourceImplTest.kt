package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.remote.implementation.UserCouponsRemoteDataSourceImpl
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

class UserCouponsRemoteDataSourceImplTest {

    @Test
    fun addCouponToUser_happyPath_validates_request_and_json_body() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond("", HttpStatusCode.Created)
        })

        val result = UserCouponsRemoteDataSourceImpl(provider).addCouponToUser(4, "WELCOME")

        assertTrue(result.isSuccess)
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("https://api.test/mobile/users/4/coupons", request.url.toString())
        assertEquals(ContentType.Application.Json.toString(), request.requestContentType())
        assertTrue(request.bodyAsText().contains("\"coupon_code\":\"WELCOME\""))
    }

    @Test
    fun getUserCoupons_returns_failure_on_http_error() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(fixtureJson("coupons", "get_user_coupons_error_standard.json"), HttpStatusCode.BadRequest, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserCouponsRemoteDataSourceImpl(provider).getUserCoupons(2)
        assertTrue(result.isFailure)
    }

    @Test
    fun getUserCoupons_returns_failure_on_malformed_json() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(fixtureJson("coupons", "get_user_coupons_malformed.json") , HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserCouponsRemoteDataSourceImpl(provider).getUserCoupons(2)
        assertTrue(result.isFailure)
    }

    @Test
    fun getUserCoupons_handles_optional_collection_field_missing() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(fixtureJson("coupons", "get_user_coupons_optional_missing.json"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserCouponsRemoteDataSourceImpl(provider).getUserCoupons(2)
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrNull()!!.first().productIds)
    }

    @Test
    fun addCouponToUser_edge_allows_blank_coupon_code() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond("", HttpStatusCode.Created)
        })

        val result = UserCouponsRemoteDataSourceImpl(provider).addCouponToUser(2, "")

        assertTrue(result.isSuccess)
        assertTrue(request.bodyAsText().contains("\"coupon_code\":\"\""))
    }
}
