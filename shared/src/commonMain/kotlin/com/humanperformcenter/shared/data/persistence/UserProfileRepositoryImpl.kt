package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.local.UserProfileLocalDataSource
import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.remote.UserProfileRemoteDataSource
import com.humanperformcenter.shared.domain.repository.UserProfileRepository

class UserProfileRepositoryImpl(
    private val remoteDataSource: UserProfileRemoteDataSource,
    private val localDataSource: UserProfileLocalDataSource,
) : UserProfileRepository {
    override suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User> =
        remoteDataSource.updateUser(user, profilePicBytes).onSuccess { localDataSource.saveUser(it) }

    override suspend fun getUserById(id: Int): Result<User> = remoteDataSource.getUserById(id)
    override suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit> = remoteDataSource.deleteProfilePic(req)
}
