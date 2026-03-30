package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.data.remote.impl.UserProfileRemoteDataSourceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserProfileRemoteDataSourceImplTest {

    @Test
    fun getUserById_sends_expected_url_method_and_deserializes_success() = runTest {
        lateinit var capturedRequest: HttpRequestData
        val provider = testProvider(
            apiEngine = MockEngine { request ->
                capturedRequest = request
                respond(
                    content = """
                        {
                          "id": 7,
                          "fullName": "Maria Lopez",
                          "email": "maria@test.com",
                          "phone": "611111111",
                          "sex": "F",
                          "dateOfBirth": "1989-10-10",
                          "postcode": 28080,
                          "postAddress": "Gran Via 10",
                          "dni": "00000000X",
                          "profilePictureName": "maria.jpg"
                        }
                    """.trimIndent(),
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
                    content = """
                        {
                          "id": 9,
                          "fullName": "Carlos Ruiz",
                          "email": "carlos@test.com",
                          "phone": "622222222",
                          "sex": "M",
                          "dateOfBirth": "1992-02-02",
                          "postcode": 28010,
                          "postAddress": "Alcala 20",
                          "dni": "11111111H",
                          "profilePictureName": "profile.jpg"
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
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

        assertTrue(result.isSuccess)
        assertEquals(HttpMethod.Put, capturedRequest.method)
        assertEquals("https://api.test/mobile/user", capturedRequest.url.toString())

        val contentType = capturedRequest.requestContentType().orEmpty()
        assertTrue(contentType.startsWith(ContentType.MultiPart.FormData.toString()))

        val multipartBody = capturedRequest.bodyAsText()
        assertTrue(multipartBody.contains("name=\"user\""))
        assertTrue(multipartBody.contains("\"fullName\":\"Carlos Ruiz\""))
        assertTrue(multipartBody.contains("name=\"profile_pic\""))
        assertTrue(multipartBody.contains("filename=\"profile.jpg\""))
    }

    @Test
    fun getUserById_returns_failure_when_response_json_is_invalid() = runTest {
        val provider = testProvider(
            apiEngine = MockEngine {
                respond(
                    content = "{\"id\":\"wrong-type\"}",
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

    private suspend fun HttpRequestData.bodyAsText(): String = when (val outgoing = body) {
        is io.ktor.http.content.OutgoingContent.ByteArrayContent -> outgoing.bytes().decodeToString()
        is io.ktor.http.content.OutgoingContent.ReadChannelContent -> outgoing.readFrom().readRemaining().readText()
        is io.ktor.http.content.OutgoingContent.WriteChannelContent -> {
            val channel = ByteChannel(autoFlush = true)
            outgoing.writeTo(channel)
            channel.close()
            channel.readRemaining().readText()
        }

        is io.ktor.http.content.OutgoingContent.NoContent -> ""
        else -> ""
    }

    private fun HttpRequestData.requestContentType(): String? {
        val fromHeaders = headers[HttpHeaders.ContentType]
        if (fromHeaders != null) return fromHeaders

        val outgoing = body
        return outgoing.contentType?.toString()
    }
}
