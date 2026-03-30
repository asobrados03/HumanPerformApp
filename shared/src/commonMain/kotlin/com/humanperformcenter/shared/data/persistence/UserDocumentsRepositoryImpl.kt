package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.remote.UserDocumentsRemoteDataSource
import com.humanperformcenter.shared.domain.repository.UserDocumentsRepository

class UserDocumentsRepositoryImpl(
    private val remoteDataSource: UserDocumentsRemoteDataSource,
) : UserDocumentsRepository {
    override suspend fun uploadDocument(userId: Int, name: String, data: ByteArray): Result<String> =
        remoteDataSource.uploadDocument(userId, name, data)
}
