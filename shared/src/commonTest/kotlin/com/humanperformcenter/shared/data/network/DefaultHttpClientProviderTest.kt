package com.humanperformcenter.shared.data.network

import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.model.auth.RefreshResponse
import com.humanperformcenter.shared.data.model.user.User
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull

class DefaultHttpClientProviderTest {

    @Test
    fun loadTokens_when_access_and_refresh_exist_adds_bearer_tokens() = runTest {
        val local = FakeAuthLocalDataSource(accessToken = "access-1", refreshToken = "refresh-1")
        val requests = mutableListOf<HttpRequestData>()

        val apiEngine = MockEngine { request ->
            requests += request
            respond("ok", HttpStatusCode.OK)
        }
        val authEngine = MockEngine { error("Refresh should not be called") }

        val provider = DefaultHttpClientProvider(
            authLocalDataSource = local,
            authClientEngine = authEngine,
            apiClientEngine = apiEngine,
        )

        runCatching {
            provider.apiClient.get { url("https://test.local/protected") }
        }

        assertEquals("Bearer access-1", requests.single().headers[HttpHeaders.Authorization])
        assertEquals(0, local.clearCount)
    }

    @Test
    fun refreshTokens_when_refresh_token_missing_clears_storage_and_emits_logout() = runTest {
        val local = FakeAuthLocalDataSource(accessToken = "expired-access", refreshToken = null)
        val logoutDeferred = CompletableDeferred<Unit>()

        val apiEngine = MockEngine { respond("unauthorized", HttpStatusCode.Unauthorized) }
        val authEngine = MockEngine { error("Refresh endpoint should not be called without refresh token") }

        val provider = DefaultHttpClientProvider(local, authEngine, apiEngine)

        val collectorJob = launch {
            provider.logoutEvents.first()
            logoutDeferred.complete(Unit)
        }

        runCatching {
            provider.apiClient.get { url("https://test.local/protected") }
        }

        logoutDeferred.await()
        collectorJob.cancel()

        assertEquals(1, local.clearCount)
        assertEquals(0, local.saveTokensCount)
        assertNull(local.accessToken)
        assertNull(local.refreshToken)
    }

    @Test
    fun refreshTokens_when_refresh_endpoint_returns_401_clears_storage_and_emits_logout() = runTest {
        val local = FakeAuthLocalDataSource(accessToken = "expired-access", refreshToken = "refresh-old")
        val logoutDeferred = CompletableDeferred<Unit>()

        val apiEngine = MockEngine { respond("unauthorized", HttpStatusCode.Unauthorized) }
        val authEngine = MockEngine { request ->
            assertEquals("/api/mobile/tokens/refresh", request.url.fullPath)
            respond("unauthorized", HttpStatusCode.Unauthorized)
        }

        val provider = DefaultHttpClientProvider(local, authEngine, apiEngine)

        val collectorJob = launch {
            provider.logoutEvents.first()
            logoutDeferred.complete(Unit)
        }

        runCatching {
            provider.apiClient.get { url("https://test.local/protected") }
        }

        logoutDeferred.await()
        collectorJob.cancel()

        assertEquals(1, local.clearCount)
        assertEquals(0, local.saveTokensCount)
    }

    @Test
    fun refreshTokens_when_refresh_succeeds_saves_and_returns_new_tokens() = runTest {
        val local = FakeAuthLocalDataSource(
            accessToken = "expired-access",
            refreshToken = "refresh-old"
        )

        val protectedAuthHeaders = mutableListOf<String?>()
        var protectedCalls = 0

        val apiEngine = MockEngine { request ->
            if (request.url.host == "test.local") {
                protectedCalls += 1
                protectedAuthHeaders += request.headers[HttpHeaders.Authorization]

                if (protectedCalls == 1) {
                    respond("unauthorized", HttpStatusCode.Unauthorized)
                } else {
                    respond("ok", HttpStatusCode.OK)
                }
            } else {
                error("Unexpected host: ${request.url.host}")
            }
        }

        val authEngine = MockEngine { request ->
            assertEquals("/api/mobile/tokens/refresh", request.url.fullPath)
            assertEquals("Bearer refresh-old", request.headers[HttpHeaders.Authorization])

            val json = Json.encodeToString(
                RefreshResponse("new-access", "new-refresh")
            )

            respond(
                content = ByteReadChannel(json),
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                ),
            )
        }

        val provider = DefaultHttpClientProvider(local, authEngine, apiEngine)

        // ✅ SIN genéricos
        provider.apiClient.get("https://test.local/protected")

        assertEquals(2, protectedCalls)
        assertEquals(
            listOf<String?>("Bearer expired-access", "Bearer new-access"),
            protectedAuthHeaders
        )
        assertEquals(1, local.saveTokensCount)
        assertEquals("new-access", local.savedAccessToken)
        assertEquals("new-refresh", local.savedRefreshToken)
        assertEquals(0, local.clearCount)
    }

    @Test
    fun refreshTokens_when_refresh_throws_exception_clears_storage_and_emits_logout() = runTest {
        val local = FakeAuthLocalDataSource(
            accessToken = "expired-access",
            refreshToken = "refresh-old"
        )
        val logoutDeferred = CompletableDeferred<Unit>()

        val apiEngine = MockEngine {
            respond("unauthorized", HttpStatusCode.Unauthorized)
        }
        val authEngine = MockEngine {
            throw RuntimeException("network timeout")
        }

        val provider = DefaultHttpClientProvider(local, authEngine, apiEngine)

        val collectorJob = launch {
            provider.logoutEvents.first()
            logoutDeferred.complete(Unit)
        }

        assertFails {
            provider.apiClient.get { url("https://test.local/protected") }
        }

        logoutDeferred.await()
        collectorJob.cancel()

        assertEquals(1, local.clearCount)
        assertEquals(0, local.saveTokensCount)
    }

    private class FakeAuthLocalDataSource(
        var accessToken: String?,
        var refreshToken: String?,
    ) : AuthLocalDataSource {
        var savedAccessToken: String? = null
        var savedRefreshToken: String? = null
        var saveTokensCount: Int = 0
        var clearCount: Int = 0

        private val tokenFlow = MutableStateFlow(accessToken ?: "")
        private val userFlowState = MutableStateFlow<User?>(null)

        override suspend fun getAccessToken(): String? = accessToken

        override suspend fun getRefreshToken(): String? = refreshToken

        override fun accessTokenFlow(): Flow<String> = tokenFlow

        override fun userFlow(): Flow<User?> = userFlowState

        override suspend fun saveTokens(accessToken: String, refreshToken: String) {
            saveTokensCount += 1
            savedAccessToken = accessToken
            savedRefreshToken = refreshToken
            this.accessToken = accessToken
            this.refreshToken = refreshToken
            tokenFlow.value = accessToken
        }

        override suspend fun clearTokens() {
            accessToken = null
            refreshToken = null
            tokenFlow.value = ""
        }

        override suspend fun saveUser(user: User) {
            userFlowState.value = user
        }

        override suspend fun clear() {
            clearCount += 1
            clearTokens()
            userFlowState.value = null
        }
    }
}
