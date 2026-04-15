package com.humanperformcenter

import com.humanperformcenter.shared.data.network.HttpClientProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
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

class MockHttpClientProvider(
    private val scenario: Scenario = Scenario.HappyPath,
) : HttpClientProvider {

    enum class Scenario {
        HappyPath,
        LoginServerError,
        SessionExpiredOnProducts,
    }

    override val baseUrl: String = "https://mock.api"

    private val logout = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val logoutEvents: SharedFlow<Unit> = logout

    private val jsonParser = Json { ignoreUnknownKeys = true }

    private val engine = MockEngine { request -> respondFor(this, request) as HttpResponseData }

    override val authClient: HttpClient = buildClient()
    override val apiClient: HttpClient = buildClient()

    private fun buildClient(): HttpClient = HttpClient(engine) {
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }

    private data class Route(
        val matches: (HttpRequestData) -> Boolean,
        val response: MockRequestHandleScope.(HttpRequestData) -> Any,
    )

    private val routes = listOf(
        Route(
            matches = { it.url.encodedPath.endsWith("/mobile/sessions") },
            response = { request ->
                if (scenario == Scenario.LoginServerError) {
                    json(this, """{"message":"server-down"}""", HttpStatusCode.InternalServerError)
                } else {
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
                            this,
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
                        json(this, "{}", HttpStatusCode.BadRequest)
                    }
                }
            }
        ),
        Route(
            matches = { it.url.encodedPath.endsWith("/mobile/services") },
            response = { _ -> json(this, """[{"id":1,"name":"Services","image":null}]""") },
        ),
        Route(
            matches = { it.url.encodedPath.endsWith("/mobile/service-products") },
            response = { _ ->
                json(
                    this,
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
            },
        ),
        Route(
            matches = { it.url.encodedPath.endsWith("/mobile/users/1/products") },
            response = { _ ->
                if (scenario == Scenario.SessionExpiredOnProducts) {
                    logout.tryEmit(Unit)
                    json(this, """{"message":"expired"}""", HttpStatusCode.Unauthorized)
                } else {
                    json(this, "[]")
                }
            },
        ),
        Route(
            matches = { it.url.encodedPath.endsWith("/mobile/users/1/coupons") },
            response = { _ -> json(this, "[]") },
        ),
        Route(
            matches = { it.url.encodedPath.endsWith("/mobile/products/100") },
            response = { _ ->
                json(
                    this,
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
            },
        ),
        Route(
            matches = { it.url.encodedPath.endsWith("/mobile/user-bookings") },
            response = { _ ->
                json(
                    this,
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
            },
        ),
        Route(matches = { it.url.encodedPath.endsWith("/mobile/daily") }, response = { _ -> json(this, "[]") }),
        Route(matches = { it.url.encodedPath.endsWith("/mobile/holidays") }, response = { _ -> json(this, "[]") }),
        Route(matches = { it.url.encodedPath.contains("/stripe/") }, response = { _ -> json(this, "{}") }),
    )

    private fun respondFor(
        scope: MockRequestHandleScope,
        request: HttpRequestData
    ): Any {
        val route = routes.firstOrNull { it.matches(request) }
        return route?.response?.invoke(scope, request) ?: json(scope, "{}")
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
