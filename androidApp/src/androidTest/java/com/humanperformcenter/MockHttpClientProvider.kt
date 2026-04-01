package com.humanperformcenter

import com.humanperformcenter.shared.data.network.HttpClientProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.Json

class MockHttpClientProvider : HttpClientProvider {
    override val baseUrl: String = "https://mock.api"
    private val logout = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val logoutEvents: SharedFlow<Unit> = logout

    private val engine = MockEngine { request: HttpRequestData -> respondFor(this, request) }

    override val authClient: HttpClient = buildClient()
    override val apiClient: HttpClient = buildClient()

    private fun buildClient(): HttpClient = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private fun respondFor(scope: MockRequestHandleScope, request: HttpRequestData) = when {
        request.url.encodedPath.endsWith("/mobile/sessions") -> {
            val body = request.body.toString()
            if (body.contains("valid@humanperform.com") && body.contains("12345678Aa")) {
                jsonResponse(scope,
                    """{"id":1,"fullName":"Test User","email":"valid@humanperform.com","phone":"600000000","sex":"M","dateOfBirth":"1990-01-01","postcode":28001,"postAddress":"Street 1","dni":"12345678A","profilePictureName":null,"accessToken":"token","refreshToken":"refresh"}"""
                )
            } else {
                jsonResponse(scope, "{}", HttpStatusCode.BadRequest)
            }
        }
        request.url.encodedPath.endsWith("/mobile/services") -> {
            jsonResponse(scope, """[{"id":1,"name":"Services","image":null}]""")
        }
        request.url.encodedPath.endsWith("/mobile/service-products") -> {
            jsonResponse(scope, """[{"id":100,"name":"Pack 8 sesiones","description":"Desc","price":30.0,"image":null,"type_of_product":"single","price_id":null,"session":8,"service_ids":[1],"isAvailable":true}]""")
        }
        request.url.encodedPath.endsWith("/mobile/users/1/products") -> jsonResponse(scope, "[]")
        request.url.encodedPath.endsWith("/mobile/users/1/coupons") -> jsonResponse(scope, "[]")
        request.url.encodedPath.endsWith("/mobile/products/100") -> {
            jsonResponse(scope, """{"id":100,"name":"Pack 8 sesiones","description":"Desc","price":30.0,"image":null,"type_of_product":"single","price_id":null,"session":8,"service_ids":[1],"isAvailable":true}""")
        }
        request.url.encodedPath.endsWith("/mobile/user-bookings") -> {
            jsonResponse(scope, """[{"id":77,"date":"2099-01-01","hour":"10:00:00","service":"Services","product":"Pack 8 sesiones","service_id":1,"product_id":100,"coach_name":"Coach Demo","coach_profile_pic":null}]""")
        }
        request.url.encodedPath.endsWith("/mobile/daily") -> jsonResponse(scope, "[]")
        request.url.encodedPath.endsWith("/mobile/holidays") -> jsonResponse(scope, "[]")
        request.url.encodedPath.contains("/stripe/") -> jsonResponse(scope, "{}")
        else -> jsonResponse(scope, "{}")
    }

    private fun jsonResponse(
        scope: MockRequestHandleScope,
        payload: String,
        code: HttpStatusCode = HttpStatusCode.OK
    ) = scope.respond(
        content = payload,
        status = code,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    )
}
