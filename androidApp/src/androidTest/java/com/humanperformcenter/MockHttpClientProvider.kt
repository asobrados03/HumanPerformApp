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
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class MockHttpClientProvider : HttpClientProvider {

    override val baseUrl: String = "https://mock.api"

    private val logout = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val logoutEvents: SharedFlow<Unit> = logout

    private val jsonParser = Json { ignoreUnknownKeys = true }

    private val engine = MockEngine { request -> respondFor(this, request) }

    override val authClient: HttpClient = buildClient()
    override val apiClient: HttpClient = buildClient()

    private fun buildClient(): HttpClient = HttpClient(engine) {
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }

    private fun respondFor(
        scope: MockRequestHandleScope,
        request: HttpRequestData
    ) = when {

        // 🔐 LOGIN
        request.url.encodedPath.endsWith("/mobile/sessions") -> {

            val bodyText = (request.body as? TextContent)?.text ?: ""

            val (email, password) = try {
                val json = jsonParser.parseToJsonElement(bodyText).jsonObject
                val email = json["email"]?.jsonPrimitive?.content
                val password = json["password"]?.jsonPrimitive?.content
                email to password
            } catch (_: Exception) {
                null to null
            }

            if (email == "valid@humanperform.com" && password == "12345678Aa") {
                json(
                    scope,
                    """{
                        "id":1,
                        "fullName":"Test User",
                        "email":"valid@humanperform.com",
                        "phone":"600000000",
                        "sex":"M",
                        "dateOfBirth":"1990-01-01",
                        "postcode":28001,
                        "postAddress":"Street 1",
                        "dni":"12345678A",
                        "profilePictureName":null,
                        "accessToken":"token",
                        "refreshToken":"refresh"
                    }"""
                )
            } else {
                json(scope, "{}", HttpStatusCode.BadRequest)
            }
        }

        // 📦 SERVICES
        request.url.encodedPath.endsWith("/mobile/services") -> {
            json(scope, """[{"id":1,"name":"Services","image":null}]""")
        }

        // 🛒 SERVICE PRODUCTS
        request.url.encodedPath.endsWith("/mobile/service-products") -> {
            json(
                scope,
                """[{
                    "id":100,
                    "name":"Pack 8 sesiones",
                    "description":"Desc",
                    "price":30.0,
                    "image":null,
                    "type_of_product":"single",
                    "price_id":null,
                    "session":8,
                    "service_ids":[1],
                    "isAvailable":true
                }]"""
            )
        }

        // 👤 USER PRODUCTS
        request.url.encodedPath.endsWith("/mobile/users/1/products") ->
            json(scope, "[]")

        // 🎟️ USER COUPONS
        request.url.encodedPath.endsWith("/mobile/users/1/coupons") ->
            json(scope, "[]")

        // 📄 PRODUCT DETAIL
        request.url.encodedPath.endsWith("/mobile/products/100") -> {
            json(
                scope,
                """{
                    "id":100,
                    "name":"Pack 8 sesiones",
                    "description":"Desc",
                    "price":30.0,
                    "image":null,
                    "type_of_product":"single",
                    "price_id":null,
                    "session":8,
                    "service_ids":[1],
                    "isAvailable":true
                }"""
            )
        }

        // 📅 BOOKINGS
        request.url.encodedPath.endsWith("/mobile/user-bookings") -> {
            json(
                scope,
                """[{
                    "id":77,
                    "date":"2099-01-01",
                    "hour":"10:00:00",
                    "service":"Services",
                    "product":"Pack 8 sesiones",
                    "service_id":1,
                    "product_id":100,
                    "coach_name":"Coach Demo",
                    "coach_profile_pic":null
                }]"""
            )
        }

        // 📆 CALENDAR AUX
        request.url.encodedPath.endsWith("/mobile/daily") ->
            json(scope, "[]")

        request.url.encodedPath.endsWith("/mobile/holidays") ->
            json(scope, "[]")

        // 💳 STRIPE
        request.url.encodedPath.contains("/stripe/") ->
            json(scope, "{}")

        // ❓ DEFAULT
        else -> json(scope, "{}")
    }

    private fun json(
        scope: MockRequestHandleScope,
        payload: String,
        code: HttpStatusCode = HttpStatusCode.OK
    ) = scope.respond(
        content = payload,
        status = code,
        headers = headersOf(
            HttpHeaders.ContentType,
            ContentType.Application.Json.toString()
        )
    )
}