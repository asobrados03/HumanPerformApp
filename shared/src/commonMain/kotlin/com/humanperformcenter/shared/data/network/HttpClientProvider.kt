package com.humanperformcenter.shared.data.network

import io.ktor.client.HttpClient

interface HttpClientProvider {
    val apiClient: HttpClient
    val authClient: HttpClient
    val baseUrl: String
}

object DefaultHttpClientProvider : HttpClientProvider {
    override val apiClient: HttpClient get() = ApiClient.apiClient
    override val authClient: HttpClient get() = ApiClient.authClient
    override val baseUrl: String get() = ApiClient.baseUrl
}
