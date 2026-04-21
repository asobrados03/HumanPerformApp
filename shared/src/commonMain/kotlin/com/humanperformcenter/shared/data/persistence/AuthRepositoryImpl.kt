package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.model.auth.LoginResponse
import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.data.model.auth.RegisterResponse
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.remote.AuthRemoteDataSource
import com.humanperformcenter.shared.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val local: AuthLocalDataSource,
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<LoginResponse> =
        run {
            println(">>> AuthRepositoryImpl.login: iniciando login para $email")
            remote.login(email, password)
        }
            .mapDomainError(ErrorCategory.AUTH)
            .onSuccess { data ->
            println(">>> AuthRepositoryImpl.login: login OK, persistiendo tokens y user id=${data.id}")
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
            local.saveTokens(data.accessToken, data.refreshToken)
            local.saveUser(userData)
            println(">>> AuthRepositoryImpl.login: saveUser completado id=${userData.id}")
            }

    override suspend fun register(data: RegisterRequest): Result<RegisterResponse> =
        remote.register(data).mapDomainError(ErrorCategory.AUTH)

    override suspend fun resetPassword(email: String): Result<Unit> =
        remote.resetPassword(email).mapDomainError(ErrorCategory.AUTH)

    override suspend fun changePassword(currentPassword: String, newPassword: String, userId: Int)
    : Result<Unit> = remote.changePassword(currentPassword, newPassword, userId)
        .mapDomainError(ErrorCategory.AUTH)

    override suspend fun logout(): Result<Unit> = remote.logout()
        .mapDomainError(ErrorCategory.AUTH)
}
