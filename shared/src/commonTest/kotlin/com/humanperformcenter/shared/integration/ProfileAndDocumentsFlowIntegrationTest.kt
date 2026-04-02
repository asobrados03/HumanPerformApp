package com.humanperformcenter.shared.integration

import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.persistence.UserDocumentsRepositoryImpl
import com.humanperformcenter.shared.data.persistence.UserProfileRepositoryImpl
import com.humanperformcenter.shared.data.remote.implementation.UserDocumentsRemoteDataSourceImpl
import com.humanperformcenter.shared.data.remote.implementation.UserProfileRemoteDataSourceImpl
import com.humanperformcenter.shared.domain.usecase.UserDocumentUseCase
import com.humanperformcenter.shared.domain.usecase.UserProfileUseCase
import integration.InMemoryUserProfileLocalDataSource
import integration.integrationProvider
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfileAndDocumentsFlowIntegrationTest {

    @Test
    fun fetch_update_profile_and_upload_documents_flow_returns_expected_state() = runTest {
        val profileLocal = InMemoryUserProfileLocalDataSource()
        val apiEngine = MockEngine { request ->
            when (request.method) {
                HttpMethod.Get if request.url.encodedPath == "/mobile/user" -> respond(
                    """
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
                              "profilePictureName": "ana.jpg"
                            }
                            """.trimIndent(),
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
                HttpMethod.Put if request.url.encodedPath == "/mobile/user" -> respond(
                    """
                            {
                              "id": 22,
                              "fullName": "Ana Integracion Updated",
                              "email": "ana@integration.test",
                              "phone": "600000001",
                              "sex": "F",
                              "dateOfBirth": "1991-01-01",
                              "postcode": 28001,
                              "postAddress": "Calle Test 99",
                              "dni": "12345678A",
                              "profilePictureName": "ana_updated.jpg"
                            }
                            """.trimIndent(),
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
                HttpMethod.Post if request.url.encodedPath == "/mobile/users/22/documents" -> respond(
                    """{"message":"uploaded"}""",
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
                else -> error("Unhandled api endpoint: ${request.method} ${request.url}")
            }
        }

        val provider = integrationProvider(apiEngine = apiEngine)
        val profileUseCase = UserProfileUseCase(
            UserProfileRepositoryImpl(UserProfileRemoteDataSourceImpl(provider), profileLocal),
        )
        val documentUseCase = UserDocumentUseCase(
            UserDocumentsRepositoryImpl(UserDocumentsRemoteDataSourceImpl(provider)),
        )

        val fetched = profileUseCase.getUserById(22)
        val updated = profileUseCase.updateUser(
            user = User(
                id = 22,
                fullName = "Ana Integracion Updated",
                email = "ana@integration.test",
                phone = "600000001",
                sex = "F",
                dateOfBirth = "1991-01-01",
                postcode = 28001,
                postAddress = "Calle Test 99",
                dni = "12345678A",
                profilePictureName = "ana_updated.jpg",
            ),
            profilePicBytes = byteArrayOf(1, 2, 3),
        )
        val upload = documentUseCase.uploadDocument(
            userId = 22,
            name = "medical-report.pdf",
            data = byteArrayOf(9, 9, 9),
        )

        assertTrue(fetched.isSuccess)
        assertEquals("Ana Integracion", fetched.getOrThrow().fullName)
        assertTrue(updated.isSuccess)
        assertEquals("Ana Integracion Updated", updated.getOrThrow().fullName)
        assertEquals("Ana Integracion Updated", profileLocal.savedUser?.fullName)
        assertTrue(upload.isSuccess)
        assertEquals("uploaded", upload.getOrThrow())
    }
}
