package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.remote.implementation.UserWalletRemoteDataSourceImpl
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

class UserWalletRemoteDataSourceImplTest {

    @Test
    fun getEwalletBalance_happyPath_validates_url_query_and_parse() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond(fixtureJson("wallet", "balance_success.json"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })

        val result = UserWalletRemoteDataSourceImpl(provider).getEwalletBalance(2)

        assertTrue(result.isSuccess)
        assertEquals(30.5, result.getOrNull())
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("https://api.test/mobile/user/e-wallet-balance?user_id=2", request.url.toString())
    }

    @Test
    fun getEwalletBalance_returns_failure_on_http_error() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(fixtureJson("wallet", "balance_error_standard.json"), HttpStatusCode.BadRequest, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserWalletRemoteDataSourceImpl(provider).getEwalletBalance(2)
        assertTrue(result.isFailure)
    }

    @Test
    fun getEwalletTransactions_returns_failure_on_malformed_or_wrong_json() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(fixtureJson("wallet", "transactions_wrong_type.json"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserWalletRemoteDataSourceImpl(provider).getEwalletTransactions(2)
        assertTrue(result.isFailure)
    }

    @Test
    fun getEwalletBalance_handles_optional_balance_absent_or_null() = runTest {
        val providerAbsent = testProvider(apiEngine = MockEngine {
            respond(fixtureJson("wallet", "balance_optional_missing.json"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val absent = UserWalletRemoteDataSourceImpl(providerAbsent).getEwalletBalance(2)
        assertTrue(absent.isSuccess)
        assertEquals(null, absent.getOrNull())

        val providerNull = testProvider(apiEngine = MockEngine {
            respond(fixtureJson("wallet", "balance_null_explicit.json"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val nullResult = UserWalletRemoteDataSourceImpl(providerNull).getEwalletBalance(2)
        assertTrue(nullResult.isSuccess)
        assertEquals(null, nullResult.getOrNull())
    }

    @Test
    fun getEwalletTransactions_edge_extracts_transactions_collection() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond(
                fixtureJson("wallet", "transactions_success.json"),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        })
        val result = UserWalletRemoteDataSourceImpl(provider).getEwalletTransactions(2)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }
}
