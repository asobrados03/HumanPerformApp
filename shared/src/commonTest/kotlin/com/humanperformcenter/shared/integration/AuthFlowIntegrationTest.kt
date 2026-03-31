package com.humanperformcenter.shared.integration

import com.humanperformcenter.shared.data.persistence.AuthRepositoryImpl
import com.humanperformcenter.shared.data.remote.implementation.AuthRemoteDataSourceImpl
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import integration.InMemoryAuthLocalDataSource
import integration.integrationProvider
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthFlowIntegrationTest {

    @Test
    fun login_persists_user_and_tokens_and_logout_clears_local_state() = runTest {
        val authLocal = InMemoryAuthLocalDataSource()
        val authEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/mobile/sessions" -> respond(
                    content = """
                        {
                          "id": 22,
                          "fullName": "Ana Integracion",
                          "email": "ana@integration.test",
                          "phone": "600000001",
                          "sex": "F",
                          "dateOfBirth": "1991-01-01",
                          "postcode": 28001,
                          "postAddress": "Calle Test 1",
                          "dni": "12345678A",
                          "profilePictureName": "ana.jpg",
                          "accessToken": "access-22",
                          "refreshToken": "refresh-22"
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                )

                "/mobile/sessions/current" -> respond(
                    content = "",
                    status = HttpStatusCode.NoContent,
                )

                else -> error("Unhandled auth endpoint: ${request.url}")
            }
        }

        val provider = integrationProvider(apiEngine = authEngine, authEngine = authEngine)
        val remote = AuthRemoteDataSourceImpl(provider, authLocal)
        val repository = AuthRepositoryImpl(remote, authLocal)
        val useCase = AuthUseCase(repository, authLocal)

        val loginResult = useCase.login("ana@integration.test", "secret")

        assertTrue(loginResult.isSuccess)
        assertEquals("access-22", authLocal.getAccessToken())
        assertEquals("refresh-22", authLocal.getRefreshToken())
        assertEquals("Ana Integracion", authLocal.userFlow().first()?.fullName)

        val logoutResult = useCase.logout()

        assertTrue(logoutResult.isSuccess)
        assertEquals("", authLocal.accessTokenFlow().first())
        assertNull(authLocal.userFlow().first())
    }
}
