package com.humanperformcenter.shared.data.repository

import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.model.auth.LoginResponse
import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.data.model.auth.RegisterResponse
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.remote.AuthRemoteDataSource
import com.humanperformcenter.shared.domain.AuthDomainError
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthRepositoryImplTest {

    @Test
    fun login_when_remote_success_saves_tokens_and_user() = runTest {
        val expected = sampleLoginResponse()
        val remote = FakeAuthRemoteDataSource(loginResult = Result.success(expected))
        val local = FakeAuthLocalDataSource()
        val repository = AuthRepositoryImpl(remote, local)

        val result = repository.login("mail@test.com", "secret")

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        assertEquals(expected.accessToken, local.savedAccessToken)
        assertEquals(expected.refreshToken, local.savedRefreshToken)
        assertEquals(
            User(
                id = expected.id,
                fullName = expected.fullName,
                email = expected.email,
                phone = expected.phone,
                sex = expected.sex,
                dateOfBirth = expected.dateOfBirth,
                postcode = expected.postcode,
                postAddress = expected.postAddress,
                dni = expected.dni,
                profilePictureName = expected.profilePictureName,
            ),
            local.savedUser,
        )
    }

    @Test
    fun login_when_backend_error_maps_to_auth_domain_error() = runTest {
        val remote = FakeAuthRemoteDataSource(
            loginResult = Result.failure(IllegalStateException("HTTP 401 Unauthorized")),
        )
        val local = FakeAuthLocalDataSource()
        val repository = AuthRepositoryImpl(remote, local)

        val result = repository.login("mail@test.com", "secret")

        assertTrue(result.isFailure)
        assertIs<AuthDomainError.SessionExpired>(result.exceptionOrNull())
        assertEquals(null, local.savedAccessToken)
        assertEquals(null, local.savedUser)
    }

    @Test
    fun login_when_network_exception_maps_to_auth_network_failure() = runTest {
        val remote = FakeAuthRemoteDataSource(
            loginResult = Result.failure(IOException("Network down")),
        )
        val repository = AuthRepositoryImpl(remote, FakeAuthLocalDataSource())

        val result = repository.login("mail@test.com", "secret")

        assertTrue(result.isFailure)
        assertIs<AuthDomainError.NetworkFailure>(result.exceptionOrNull())
    }

    private class FakeAuthRemoteDataSource(
        private val loginResult: Result<LoginResponse> = Result.success(sampleLoginResponse()),
        private val registerResult: Result<RegisterResponse> = Result.success(RegisterResponse("ok")),
        private val resetPasswordResult: Result<Unit> = Result.success(Unit),
        private val changePasswordResult: Result<Unit> = Result.success(Unit),
        private val logoutResult: Result<Unit> = Result.success(Unit),
    ) : AuthRemoteDataSource {
        override suspend fun login(email: String, password: String): Result<LoginResponse> = loginResult
        override suspend fun register(data: RegisterRequest): Result<RegisterResponse> = registerResult
        override suspend fun resetPassword(email: String): Result<Unit> = resetPasswordResult
        override suspend fun changePassword(currentPassword: String, newPassword: String, userId: Int): Result<Unit> =
            changePasswordResult

        override suspend fun logout(): Result<Unit> = logoutResult
    }

    private class FakeAuthLocalDataSource : AuthLocalDataSource {
        var savedAccessToken: String? = null
        var savedRefreshToken: String? = null
        var savedUser: User? = null
        private val tokenFlow = MutableStateFlow("")
        private val currentUserFlow = MutableStateFlow<User?>(null)

        override suspend fun getAccessToken(): String? = savedAccessToken
        override suspend fun getRefreshToken(): String? = savedRefreshToken
        override fun accessTokenFlow(): Flow<String> = tokenFlow
        override fun userFlow(): Flow<User?> = currentUserFlow

        override suspend fun saveTokens(accessToken: String, refreshToken: String) {
            savedAccessToken = accessToken
            savedRefreshToken = refreshToken
            tokenFlow.value = accessToken
        }

        override suspend fun clearTokens() {
            savedAccessToken = null
            savedRefreshToken = null
            tokenFlow.value = ""
        }

        override suspend fun saveUser(user: User) {
            savedUser = user
            currentUserFlow.value = user
        }

        override suspend fun clearUser() {
            savedUser = null
            currentUserFlow.value = null
        }

        override suspend fun clearSession() {
            clearTokens()
            clearUser()
        }
    }

    private companion object {
        fun sampleLoginResponse() = LoginResponse(
            id = 5,
            fullName = "Ana Pérez",
            email = "ana@test.com",
            phone = "600000000",
            sex = "F",
            dateOfBirth = "1995-05-01",
            postcode = 28001,
            postAddress = "Calle Mayor 1",
            dni = "12345678A",
            profilePictureName = "ana.jpg",
            accessToken = "access-token",
            refreshToken = "refresh-token",
        )
    }
}
