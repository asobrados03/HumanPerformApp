package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.implementation.UserProfileRemoteDataSourceImpl
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserProfileRemoteDataSourceImplTest {

    @Test
    fun getUserById_sends_expected_url_method_and_deserializes_success() = runTest {
        lateinit var capturedRequest: HttpRequestData
        val provider = testProvider(
            apiEngine = MockEngine { request ->
                capturedRequest = request
                respond(
                    content = fixtureJson("profile", "user_get_success.json"),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            },
        )

        val dataSource = UserProfileRemoteDataSourceImpl(provider)
        val result = dataSource.getUserById(7)

        assertTrue(result.isSuccess)
        assertEquals(HttpMethod.Get, capturedRequest.method)
        assertEquals("https://api.test/mobile/user?user_id=7", capturedRequest.url.toString())
        assertEquals(
            User(
                id = 7,
                fullName = "Maria Lopez",
                email = "maria@test.com",
                phone = "611111111",
                sex = "F",
                dateOfBirth = "1989-10-10",
                postcode = 28080,
                postAddress = "Gran Via 10",
                dni = "00000000X",
                profilePictureName = "maria.jpg",
            ),
            result.getOrNull(),
        )
    }

    @Test
    fun updateUser_sends_multipart_body_with_json_part_and_photo() = runTest {
        lateinit var capturedRequest: HttpRequestData

        val provider = testProvider(
            apiEngine = MockEngine { request ->
                capturedRequest = request
                respond(
                    content = fixtureJson("profile", "user_update_success.json"),
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    ),
                )
            },
        )

        val dataSource = UserProfileRemoteDataSourceImpl(provider)

        val user = User(
            id = 9,
            fullName = "Carlos Ruiz",
            email = "carlos@test.com",
            phone = "622222222",
            sex = "M",
            dateOfBirth = "1992-02-02",
            postcode = 28010,
            postAddress = "Alcala 20",
            dni = "11111111H",
            profilePictureName = "profile.jpg",
        )

        val result = dataSource.updateUser(user, profilePicBytes = byteArrayOf(8, 9, 10))

        // ✅ Resultado
        assertTrue(result.isSuccess)

        // ✅ Método y URL
        assertEquals(HttpMethod.Put, capturedRequest.method)
        assertEquals("https://api.test/mobile/user", capturedRequest.url.toString())

        // ✅ Content-Type (forma correcta)
        val body = capturedRequest.body
        val contentType = body.contentType
        assertNotNull(contentType)
        assertTrue(contentType.toString().startsWith("multipart/form-data"))

        // ✅ Tipo de body
        assertTrue(capturedRequest.body is OutgoingContent.WriteChannelContent)

        // ✅ Leer multipart REAL
        val content = capturedRequest.body as OutgoingContent.WriteChannelContent
        val channel = ByteChannel(autoFlush = true)
        content.writeTo(channel)
        val bodyText = channel.readRemaining().readText()

        // ✅ Validaciones ROBUSTAS (no frágiles)
        with(bodyText) {
            // Parte JSON
            assertTrue(contains("user"))
            assertTrue(contains("Carlos Ruiz"))
            assertTrue(contains("carlos@test.com"))

            // Parte imagen
            assertTrue(contains("profile_pic"))
            assertTrue(contains("profile.jpg"))
        }
    }

    @Test
    fun updateUser_omits_profile_pic_part_when_bytes_are_null() = runTest {
        lateinit var capturedRequest: HttpRequestData

        val provider = testProvider(
            apiEngine = MockEngine { request ->
                capturedRequest = request
                respond(
                    content = fixtureJson("profile", "user_update_success.json"),
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    ),
                )
            },
        )

        val dataSource = UserProfileRemoteDataSourceImpl(provider)
        val user = User(
            id = 9,
            fullName = "Carlos Ruiz",
            email = "carlos@test.com",
            phone = "622222222",
            sex = "M",
            dateOfBirth = "1992-02-02",
            postcode = 28010,
            postAddress = "Alcala 20",
            dni = "11111111H",
            profilePictureName = null,
        )

        val result = dataSource.updateUser(user, profilePicBytes = null)
        assertTrue(result.isSuccess)

        val content = capturedRequest.body as OutgoingContent.WriteChannelContent
        val channel = ByteChannel(autoFlush = true)
        content.writeTo(channel)
        val bodyText = channel.readRemaining().readText()

        assertTrue(bodyText.contains("name=\"user\"") || bodyText.contains("name=user"))
        assertFalse(bodyText.contains("name=\"profile_pic\""))
    }

    @Test
    fun getUserById_returns_failure_when_response_json_is_invalid() = runTest {
        val provider = testProvider(
            apiEngine = MockEngine {
                respond(
                    content = fixtureJson("profile", "user_get_wrong_type.json"),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            },
        )

        val dataSource = UserProfileRemoteDataSourceImpl(provider)
        val result = dataSource.getUserById(99)

        assertTrue(result.isFailure)
    }

    private fun testProvider(apiEngine: MockEngine): HttpClientProvider {
        val json = Json { ignoreUnknownKeys = true }
        val apiClient = HttpClient(apiEngine) {
            install(ContentNegotiation) { json(json) }
        }

        return object : HttpClientProvider {
            override val apiClient: HttpClient = apiClient
            override val authClient: HttpClient = apiClient
            override val baseUrl: String = "https://api.test"
            override val logoutEvents: SharedFlow<Unit> = MutableSharedFlow()
        }
    }

}
