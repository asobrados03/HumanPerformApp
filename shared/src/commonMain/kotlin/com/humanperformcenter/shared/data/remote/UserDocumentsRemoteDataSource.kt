package com.humanperformcenter.shared.data.remote

interface UserDocumentsRemoteDataSource {
    suspend fun uploadDocument(userId: Int, name: String, data: ByteArray): Result<String>
}
