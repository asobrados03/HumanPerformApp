package com.humanperformcenter.shared.data.remote

import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.user.User

interface UserProfileRemoteDataSource {
    suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User>
    suspend fun getUserById(id: Int): Result<User>
    suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit>
}
