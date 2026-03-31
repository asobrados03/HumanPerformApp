package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.remote.UserDocumentsRemoteDataSource
import com.humanperformcenter.shared.domain.DomainException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UserDocumentsRepositoryImplTest {

    @Test
    fun upload_document_when_success_propagates_expected_url() = runTest {
        val repository = UserDocumentsRepositoryImpl(
            FakeUserDocumentsRemoteDataSource(uploadDocumentResult = Result.success("https://cdn/doc.pdf")),
        )

        val result = repository.uploadDocument(userId = 4, name = "doc.pdf", data = byteArrayOf(1, 2, 3))

        assertTrue(result.isSuccess)
        assertEquals("https://cdn/doc.pdf", result.getOrNull())
    }

    @Test
    fun upload_document_when_remote_error_maps_to_domain_server() = runTest {
        val repository = UserDocumentsRepositoryImpl(
            FakeUserDocumentsRemoteDataSource(
                uploadDocumentResult = Result.failure(IllegalStateException("HTTP 502 Bad Gateway")),
            ),
        )

        val result = repository.uploadDocument(userId = 4, name = "doc.pdf", data = byteArrayOf(1))

        assertTrue(result.isFailure)
        assertIs<DomainException.Server>(result.exceptionOrNull())
    }

    @Test
    fun upload_document_when_remote_returns_empty_location_propagates_empty_string() = runTest {
        val repository = UserDocumentsRepositoryImpl(
            FakeUserDocumentsRemoteDataSource(uploadDocumentResult = Result.success("")),
        )

        val result = repository.uploadDocument(userId = 4, name = "doc.pdf", data = byteArrayOf())

        assertTrue(result.isSuccess)
        assertEquals("", result.getOrNull())
    }

    @Test
    fun upload_document_when_contract_edge_empty_payload_is_forwarded() = runTest {
        val remote = FakeUserDocumentsRemoteDataSource(uploadDocumentResult = Result.success("ok"))
        val repository = UserDocumentsRepositoryImpl(remote)
        val payload = byteArrayOf()

        val result = repository.uploadDocument(userId = 1, name = "empty.bin", data = payload)

        assertTrue(result.isSuccess)
        assertContentEquals(payload, remote.lastPayload)
    }

    private class FakeUserDocumentsRemoteDataSource(
        private val uploadDocumentResult: Result<String> = Result.success("ok"),
    ) : UserDocumentsRemoteDataSource {
        var lastPayload: ByteArray? = null

        override suspend fun uploadDocument(userId: Int, name: String, data: ByteArray): Result<String> {
            lastPayload = data
            return uploadDocumentResult
        }
    }
}
