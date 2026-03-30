package com.humanperformcenter.shared.data.repository

import com.humanperformcenter.shared.data.local.UserProfileLocalDataSource
import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.remote.UserProfileRemoteDataSource
import com.humanperformcenter.shared.domain.repository.UserProfileRepository

class UserProfileRepositoryImpl(
    private val remote: UserProfileRemoteDataSource,
    private val local: UserProfileLocalDataSource,
) : UserProfileRepository {
    override suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User> =
        remote.updateUser(user, profilePicBytes).onSuccess { local.saveUser(it) }

    override suspend fun getUserById(id: Int): Result<User> = remote.getUserById(id)
    override suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit> = remote.deleteProfilePic(req)
}
