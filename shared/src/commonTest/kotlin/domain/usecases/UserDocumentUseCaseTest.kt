package com.humanperformcenter.shared.domain.usecases

import com.humanperformcenter.shared.domain.repository.UserDocumentsRepository
import com.humanperformcenter.shared.domain.usecase.UserDocumentUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserDocumentUseCaseTest {
    private class FakeUserDocumentsRepository(
        private val uploadResult: Result<String>
    ) : UserDocumentsRepository {
        override suspend fun uploadDocument(userId: Int, name: String, data: ByteArray): Result<String> = uploadResult
    }

    @Test
    fun userDocumentUseCase_whenUploadSucceeds_returnsMessage() = runBlocking {
        // Arrange
        val useCase = UserDocumentUseCase(FakeUserDocumentsRepository(Result.success("OK")))

        // Act
        val result = useCase.uploadDocument(1, "dni.pdf", byteArrayOf(1))

        // Assert
        assertEquals("OK", result.getOrNull())
    }

    @Test
    fun userDocumentUseCase_whenRepositoryReturnsEmptyMessage_returnsEmptyMessage() = runBlocking {
        // Arrange
        val useCase = UserDocumentUseCase(FakeUserDocumentsRepository(Result.success("")))

        // Act
        val result = useCase.uploadDocument(1, "empty.txt", byteArrayOf())

        // Assert
        assertEquals("", result.getOrNull())
    }

    @Test
    fun userDocumentUseCase_whenRepositoryFails_propagatesFailure() = runBlocking {
        // Arrange
        val useCase = UserDocumentUseCase(FakeUserDocumentsRepository(Result.failure(RuntimeException("error"))))

        // Act
        val result = useCase.uploadDocument(1, "dni.pdf", byteArrayOf(1))

        // Assert
        assertTrue(result.isFailure)
    }
}
