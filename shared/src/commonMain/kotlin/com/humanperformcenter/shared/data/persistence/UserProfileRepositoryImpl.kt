package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.remote.UserProfileRemoteDataSource
import com.humanperformcenter.shared.domain.repository.UserProfileRepository
import com.humanperformcenter.shared.domain.storage.SecureStorage

class UserProfileRepositoryImpl(
    private val remoteDataSource: UserProfileRemoteDataSource,
) : UserProfileRepository {
    override suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User> =
        remoteDataSource.updateUser(user, profilePicBytes).onSuccess { SecureStorage.saveUser(it) }

    override suspend fun getUserById(id: Int): Result<User> = remoteDataSource.getUserById(id)
    override suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit> = remoteDataSource.deleteProfilePic(req)
}
