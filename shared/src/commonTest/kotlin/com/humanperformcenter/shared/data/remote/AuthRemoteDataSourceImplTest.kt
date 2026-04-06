package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.model.auth.LoginResponse
import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.data.model.auth.RegisterResponse
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.implementation.AuthRemoteDataSourceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.core.readText
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthRemoteDataSourceImplTest {

    @Test
    fun login_builds_expected_request_and_deserializes_success() = runTest {
        lateinit var capturedRequest: HttpRequestData
        val provider = testProvider(
            authEngine = MockEngine { request ->
                capturedRequest = request
                respond(
                    content = fixtureJson("auth", "sessions_login_success.json"),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            },
        )

        val dataSource = AuthRemoteDataSourceImpl(provider, FakeAuthLocalDataSource())
        val result = dataSource.login(email = "ana@test.com", password = "secret")

        assertTrue(result.isSuccess)
        assertEquals(HttpMethod.Post, capturedRequest.method)
        assertEquals("https://api.test/mobile/sessions", capturedRequest.url.toString())
        assertEquals(ContentType.Application.Json.toString(), capturedRequest.requestContentType())

        val body = capturedRequest.bodyAsText()
        assertTrue(body.contains("\"email\":\"ana@test.com\""))
        assertTrue(body.contains("\"password\":\"secret\""))

        assertEquals(
            LoginResponse(
                id = 1,
                fullName = "Ana Perez",
                email = "ana@test.com",
                phone = "600000000",
                sex = "F",
                dateOfBirth = "1990-01-01",
                postcode = 28001,
                postAddress = "Calle Mayor",
                dni = "12345678A",
                profilePictureName = "ana.jpg",
                accessToken = "access-token",
                refreshToken = "refresh-token",
            ),
            result.getOrNull(),
        )
    }

    @Test
    fun login_returns_failure_when_backend_returns_error_status() = runTest {
        val provider = testProvider(
            authEngine = MockEngine {
                respond(
                    content = fixtureJson("auth", "sessions_error_standard.json"),
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            },
        )

        val dataSource = AuthRemoteDataSourceImpl(provider, FakeAuthLocalDataSource())
        val result = dataSource.login(email = "ana@test.com", password = "bad-secret")

        assertTrue(result.isFailure)
        assertIs<IllegalStateException>(result.exceptionOrNull())
        assertEquals(result.exceptionOrNull()?.message?.contains("HTTP 401"), true)
    }

    @Test
    fun register_sends_multipart_request_and_deserializes_success() = runTest {
        lateinit var capturedRequest: HttpRequestData

        val provider = testProvider(
            authEngine = MockEngine { request ->
                capturedRequest = request
                respond(
                    content = fixtureJson("auth", "users_register_success.json"),
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    ),
                )
            },
        )

        val dataSource = AuthRemoteDataSourceImpl(provider, FakeAuthLocalDataSource())

        val request = RegisterRequest(
            name = "Ana",
            surnames = "Perez",
            email = "ana@test.com",
            phone = "600000000",
            password = "secret",
            sex = "F",
            dateOfBirth = "1990-01-01",
            postCode = "28001",
            postAddress = "Calle Mayor",
            dni = "12345678A",
            deviceType = "android",
            profilePicBytes = byteArrayOf(1, 2, 3),
            profilePicName = "avatar.jpg",
        )

        val result = dataSource.register(request)

        // ✅ Resultado correcto
        assertTrue(result.isSuccess)
        assertEquals(RegisterResponse("ok"), result.getOrNull())

        // ✅ Método y URL
        assertEquals(HttpMethod.Post, capturedRequest.method)
        assertEquals("https://api.test/mobile/users", capturedRequest.url.toString())

        // ✅ Content-Type
        val body = capturedRequest.body
        val contentType = body.contentType
        assertNotNull(contentType)
        assertTrue(contentType.toString().startsWith("multipart/form-data"))

        // ✅ Tipo de body
        assertTrue(capturedRequest.body is OutgoingContent.WriteChannelContent)

        // ✅ Leer el multipart REAL (forma correcta)
        val content = capturedRequest.body as OutgoingContent.WriteChannelContent
        val channel = ByteChannel(autoFlush = true)
        content.writeTo(channel)

        val bodyText = channel.readRemaining().readText()

        // ✅ Assertions reales sobre el contenido
        assertTrue(bodyText.contains("nombre"))
        assertTrue(bodyText.contains("Ana"))

        assertTrue(bodyText.contains("profile_pic"))
        assertTrue(bodyText.contains("avatar.jpg"))

        val deviceTypePartRegex =
            Regex("name=\"device_type\"[\\s\\S]*?\\r\\n\\r\\nandroid\\r\\n")
        assertTrue(deviceTypePartRegex.containsMatchIn(bodyText))

        val rawEmailPartRegex =
            Regex("name=\"rawEmail\"[\\s\\S]*?\\r\\n\\r\\nana@test.com\\r\\n")
        assertTrue(rawEmailPartRegex.containsMatchIn(bodyText))

        val rawDobPartRegex =
            Regex("name=\"fechaNacimientoRaw\"[\\s\\S]*?\\r\\n\\r\\n01011990\\r\\n")
        assertTrue(rawDobPartRegex.containsMatchIn(bodyText))

        val postCodeAliasPartRegex =
            Regex("name=\"codigoPostal\"[\\s\\S]*?\\r\\n\\r\\n28001\\r\\n")
        assertTrue(postCodeAliasPartRegex.containsMatchIn(bodyText))

        val addressAliasPartRegex =
            Regex("name=\"direccionPostal\"[\\s\\S]*?\\r\\n\\r\\nCalle Mayor\\r\\n")
        assertTrue(addressAliasPartRegex.containsMatchIn(bodyText))

        val deviceTypeAliasPartRegex =
            Regex("name=\"deviceType\"[\\s\\S]*?\\r\\n\\r\\nandroid\\r\\n")
        assertTrue(deviceTypeAliasPartRegex.containsMatchIn(bodyText))
    }

    @Test
    fun logout_sends_bearer_token_in_auth_header() = runTest {
        lateinit var capturedRequest: HttpRequestData
        val localDataSource = FakeAuthLocalDataSource(accessToken = "abc.123")
        val provider = testProvider(
            authEngine = MockEngine { request ->
                capturedRequest = request
                respond(content = "", status = HttpStatusCode.NoContent)
            },
        )

        val dataSource = AuthRemoteDataSourceImpl(provider, localDataSource)
        val result = dataSource.logout()

        assertTrue(result.isSuccess)
        assertEquals(HttpMethod.Delete, capturedRequest.method)
        assertEquals("https://api.test/mobile/sessions/current", capturedRequest.url.toString())
        assertEquals("Bearer abc.123", capturedRequest.headers[HttpHeaders.Authorization])
    }

    @Test
    fun toLegacyFechaNacimientoRaw_converts_iso_to_ddMMyyyy() {
        val dataSource = AuthRemoteDataSourceImpl(
            testProvider(MockEngine { respond("", HttpStatusCode.OK) }),
            FakeAuthLocalDataSource(),
        )
        assertEquals("11012001", dataSource.toLegacyFechaNacimientoRaw("2001-01-11"))
    }

    @Test
    fun toLegacyFechaNacimientoRaw_converts_masked_to_ddMMyyyy() {
        val dataSource = AuthRemoteDataSourceImpl(
            testProvider(MockEngine { respond("", HttpStatusCode.OK) }),
            FakeAuthLocalDataSource(),
        )
        assertEquals("11012001", dataSource.toLegacyFechaNacimientoRaw("11/01/2001"))
    }

    private fun testProvider(authEngine: MockEngine, apiEngine: MockEngine = authEngine): HttpClientProvider {
        val json = Json { ignoreUnknownKeys = true }
        val authClient = HttpClient(authEngine) {
            install(ContentNegotiation) { json(json) }
        }
        val apiClient = HttpClient(apiEngine) {
            install(ContentNegotiation) { json(json) }
        }

        return object : HttpClientProvider {
            override val apiClient: HttpClient = apiClient
            override val authClient: HttpClient = authClient
            override val baseUrl: String = "https://api.test"
            override val logoutEvents: SharedFlow<Unit> = MutableSharedFlow()
        }
    }

    private suspend fun HttpRequestData.bodyAsText(): String = when (val outgoing = body) {
        is OutgoingContent.ByteArrayContent -> outgoing.bytes().decodeToString()
        is OutgoingContent.ReadChannelContent -> outgoing.readFrom().readRemaining().readText()
        is OutgoingContent.WriteChannelContent -> {
            val channel = ByteChannel(autoFlush = true)
            outgoing.writeTo(channel)
            channel.close()
            channel.readRemaining().readText()
        }

        is OutgoingContent.NoContent -> ""
        else -> ""
    }

    private fun HttpRequestData.requestContentType(): String? {
        val fromHeaders = headers[HttpHeaders.ContentType]
        if (fromHeaders != null) return fromHeaders

        val outgoing = body
        return outgoing.contentType?.toString()
    }

    private class FakeAuthLocalDataSource(
        private val accessToken: String? = null,
        private val refreshToken: String? = null,
    ) : AuthLocalDataSource {
        override suspend fun getAccessToken(): String? = accessToken
        override suspend fun getRefreshToken(): String? = refreshToken
        override fun accessTokenFlow(): Flow<String> = MutableStateFlow(accessToken.orEmpty())
        override fun userFlow(): Flow<User?> = MutableStateFlow(null)
        override suspend fun saveTokens(accessToken: String, refreshToken: String) = Unit
        override suspend fun clearTokens() = Unit
        override suspend fun saveUser(user: User) = Unit
        override suspend fun clear() = Unit
    }
}
