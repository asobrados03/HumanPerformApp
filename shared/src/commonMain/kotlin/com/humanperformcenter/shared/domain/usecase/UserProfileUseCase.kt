package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.repository.UserProfileRepository

class UserProfileUseCase(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User> {
        return userProfileRepository.updateUser(user, profilePicBytes)
    }

    suspend fun getUserById(id: Int): Result<User> {
        return userProfileRepository.getUserById(id)
    }

    suspend fun deleteProfilePicture(req: DeleteProfilePicRequest): Result<Unit> {
        return userProfileRepository.deleteProfilePic(req)
    }
}
