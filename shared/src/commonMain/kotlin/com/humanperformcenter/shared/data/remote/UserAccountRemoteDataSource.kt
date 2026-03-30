package com.humanperformcenter.shared.data.remote

interface UserAccountRemoteDataSource {
    suspend fun deleteUser(email: String): Result<Unit>
}
