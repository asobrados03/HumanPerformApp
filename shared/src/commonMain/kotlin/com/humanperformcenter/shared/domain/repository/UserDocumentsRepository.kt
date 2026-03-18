package com.humanperformcenter.shared.domain.repository

interface UserDocumentsRepository {
    suspend fun uploadDocument(userId: Int, name: String, data: ByteArray): Result<String>
}
