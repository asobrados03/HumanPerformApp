package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.auth.LoginResponse
import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.data.model.auth.RegisterResponse
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.remote.AuthRemoteDataSource
import com.humanperformcenter.shared.domain.repository.AuthRepository
import com.humanperformcenter.shared.domain.storage.SecureStorage

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<LoginResponse> =
        remoteDataSource.login(email, password).onSuccess { data ->
            val userData = User(
                id = data.id,
                fullName = data.fullName,
                email = data.email,
                phone = data.phone,
                sex = data.sex,
                dateOfBirth = data.dateOfBirth,
                postcode = data.postcode,
                postAddress = data.postAddress,
                dni = data.dni,
                profilePictureName = data.profilePictureName,
            )
            SecureStorage.saveTokens(data.accessToken, data.refreshToken)
            SecureStorage.saveUser(userData)
        }

    override suspend fun register(data: RegisterRequest): Result<RegisterResponse> = remoteDataSource.register(data)
    override suspend fun resetPassword(email: String): Result<Unit> = remoteDataSource.resetPassword(email)
    override suspend fun changePassword(currentPassword: String, newPassword: String, userId: Int): Result<Unit> =
        remoteDataSource.changePassword(currentPassword, newPassword, userId)
    override suspend fun logout(): Result<Unit> = remoteDataSource.logout()
}
