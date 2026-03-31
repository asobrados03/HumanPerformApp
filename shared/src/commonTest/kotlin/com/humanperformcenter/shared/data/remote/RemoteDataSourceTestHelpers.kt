package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.network.HttpClientProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.core.readText
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.Json

internal fun testProvider(authEngine: MockEngine? = null, apiEngine: MockEngine): HttpClientProvider {
    val json = Json { ignoreUnknownKeys = true }
    val apiClient = HttpClient(apiEngine) {
        expectSuccess = true
        install(ContentNegotiation) { json(json) }
    }
    val authClient = HttpClient(authEngine ?: apiEngine) {
        expectSuccess = true
        install(ContentNegotiation) { json(json) }
    }

    return object : HttpClientProvider {
        override val apiClient: HttpClient = apiClient
        override val authClient: HttpClient = authClient
        override val baseUrl: String = "https://api.test"
        override val logoutEvents: SharedFlow<Unit> = MutableSharedFlow()
    }
}

internal suspend fun HttpRequestData.bodyAsText(): String = when (val outgoing = body) {
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

internal fun HttpRequestData.requestContentType(): String? {
    val fromHeaders = headers[HttpHeaders.ContentType]
    if (fromHeaders != null) return fromHeaders
    return body.contentType?.toString()
}

internal suspend fun HttpRequestData.multipartBodyAsText(): String {
    val content = body as OutgoingContent.WriteChannelContent
    val channel = ByteChannel(autoFlush = true)
    content.writeTo(channel)
    return channel.readRemaining().readText()
}
