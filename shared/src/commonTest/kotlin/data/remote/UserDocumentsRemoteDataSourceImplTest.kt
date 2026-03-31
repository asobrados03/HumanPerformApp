package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.remote.implementation.UserDocumentsRemoteDataSourceImpl
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserDocumentsRemoteDataSourceImplTest {

    @Test
    fun uploadDocument_happyPath_validates_url_method_multipart_and_parse() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond("""{"message":"uploaded"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })

        val result = UserDocumentsRemoteDataSourceImpl(provider).uploadDocument(5, "report.pdf", byteArrayOf(1, 2))

        assertTrue(result.isSuccess)
        assertEquals("uploaded", result.getOrNull())
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("https://api.test/mobile/users/5/documents", request.url.toString())
        assertTrue(request.body is OutgoingContent.WriteChannelContent)
        val body = request.multipartBodyAsText()
        assertTrue(body.contains("filename=\"report.pdf\""))
        assertTrue(body.contains("application/pdf"))
    }

    @Test
    fun uploadDocument_returns_failure_on_http_error() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("{}", HttpStatusCode.BadRequest, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserDocumentsRemoteDataSourceImpl(provider).uploadDocument(1, "a.pdf", byteArrayOf(1))
        assertTrue(result.isFailure)
    }

    @Test
    fun uploadDocument_returns_failure_on_wrong_json_type() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("""{"message":123}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserDocumentsRemoteDataSourceImpl(provider).uploadDocument(1, "a.pdf", byteArrayOf(1))
        assertTrue(result.isFailure)
    }

    @Test
    fun uploadDocument_handles_optional_fields_absent_in_response() = runTest {
        val provider = testProvider(apiEngine = MockEngine {
            respond("""{"message":"ok"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })
        val result = UserDocumentsRemoteDataSourceImpl(provider).uploadDocument(1, "a.unknown", byteArrayOf(1))
        assertTrue(result.isSuccess)
    }

    @Test
    fun uploadDocument_edge_unknown_extension_uses_octet_stream() = runTest {
        lateinit var request: HttpRequestData
        val provider = testProvider(apiEngine = MockEngine {
            request = it
            respond("""{"message":"ok"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        })

        val result = UserDocumentsRemoteDataSourceImpl(provider).uploadDocument(9, "noext", byteArrayOf(9))

        assertTrue(result.isSuccess)
        assertTrue(request.multipartBodyAsText().contains("application/octet-stream"))
    }
}
