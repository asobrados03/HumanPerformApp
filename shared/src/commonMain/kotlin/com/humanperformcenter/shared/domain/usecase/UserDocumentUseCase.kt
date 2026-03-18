package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.domain.repository.UserDocumentsRepository

class UserDocumentUseCase(
    private val userDocumentsRepository: UserDocumentsRepository,
) {
    suspend fun uploadDocument(userId: Int, name: String, data: ByteArray): Result<String> {
        return userDocumentsRepository.uploadDocument(userId, name, data)
    }
}
