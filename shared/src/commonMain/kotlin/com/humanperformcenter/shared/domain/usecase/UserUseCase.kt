package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.User
import com.humanperformcenter.shared.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class UserUseCase(private val userRepository: UserRepository) {
    suspend fun updateUser(user: User): Result<User> = withContext(Dispatchers.IO) {
        return@withContext userRepository.updateUser(user)
    }

    suspend fun deleteUser(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext userRepository.deleteUser(email)
    }
    suspend fun getUserAllowedServices(customerId: Int): List<Int> = withContext(Dispatchers.IO) {
        return@withContext userRepository.getUserAllowedServices(customerId)
    }

}