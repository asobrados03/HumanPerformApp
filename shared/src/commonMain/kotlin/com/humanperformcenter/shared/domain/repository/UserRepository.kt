package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.User

interface UserRepository {
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteUser(email: String): Result<Unit>
    suspend fun getUserAllowedServices(customerId: Int) : List<Int>
}