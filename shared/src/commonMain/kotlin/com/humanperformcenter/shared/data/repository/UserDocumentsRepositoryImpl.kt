package com.humanperformcenter.shared.data.repository

import com.humanperformcenter.shared.data.remote.UserDocumentsRemoteDataSource
import com.humanperformcenter.shared.domain.repository.UserDocumentsRepository

class UserDocumentsRepositoryImpl(
    private val remote: UserDocumentsRemoteDataSource,
) : UserDocumentsRepository {
    override suspend fun uploadDocument(userId: Int, name: String, data: ByteArray): Result<String> =
        remote.uploadDocument(userId, name, data)
}
