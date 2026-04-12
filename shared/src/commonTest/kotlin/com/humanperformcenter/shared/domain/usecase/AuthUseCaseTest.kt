package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.model.auth.LoginResponse
import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.data.model.auth.RegisterResponse
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.repository.AuthRepository
import com.humanperformcenter.shared.domain.usecase.validation.ChangePasswordException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthUseCaseTest {

    @Test
    fun login_whenCredentialsAreValid_returnsLoginResponse() = runTest {
        val expected = LoginResponse(1, "Ana", "ana@mail.com", "600", "F", "1990-01-01", 28001, "Calle 1", null, null, "at", "rt")
        val useCase = buildUseCase(FakeAuthRepository(loginResult = Result.success(expected)), FakeAuthLocalDataSource())
        assertEquals(expected, useCase.login("ana@mail.com", "Secret123").getOrNull())
    }

    @Test
    fun register_whenDataIsValid_returnsRegisterResponse() = runTest {
        val expected = RegisterResponse("ok")
        val useCase = buildUseCase(FakeAuthRepository(registerResult = Result.success(expected)), FakeAuthLocalDataSource())
        assertEquals(expected, useCase.register(sampleRequest()).getOrNull())
    }

    @Test
    fun resetPassword_whenEmailIsValid_returnsSuccess() = runTest {
        val useCase = buildUseCase(FakeAuthRepository(resetPasswordResult = Result.success(Unit)), FakeAuthLocalDataSource())
        assertTrue(useCase.resetPassword("ana@mail.com").isSuccess)
    }

    @Test
    fun logout_whenRepositoryResponds_clearsLocalAuthAndReturnsResult() = runTest {
        val local = FakeAuthLocalDataSource()
        val useCase = buildUseCase(FakeAuthRepository(logoutResult = Result.success(Unit)), local)
        val result = useCase.logout()
        assertTrue(result.isSuccess)
        assertTrue(local.clearCalled)
    }

    @Test
    fun changePassword_whenNewPasswordIsEmpty_returnsNewRequired() = runTest {
        val useCase = buildUseCase(FakeAuthRepository(), FakeAuthLocalDataSource())
        val result = useCase.changePassword("Actual123", "", "", 1)
        assertTrue(result.isFailure)
        assertIs<ChangePasswordException.NewRequired>(result.exceptionOrNull())
    }

    @Test
    fun changePassword_whenConfirmDoesNotMatch_returnsNotMatching() = runTest {
        val useCase = buildUseCase(FakeAuthRepository(), FakeAuthLocalDataSource())
        val result = useCase.changePassword("Actual123", "Nueva123A", "Nueva123B", 1)
        assertTrue(result.isFailure)
        assertIs<ChangePasswordException.NotMatching>(result.exceptionOrNull())
    }

    @Test
    fun changePassword_whenRepositoryFails_returnsRepoFailure() = runTest {
        val repoError = IllegalStateException("network down")
        val useCase = buildUseCase(
            FakeAuthRepository(changePasswordResult = Result.failure(repoError)),
            FakeAuthLocalDataSource()
        )
        val result = useCase.changePassword("Actual123", "Nueva123A", "Nueva123A", 42)
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertIs<ChangePasswordException.RepoFailure>(exception)
        assertEquals(repoError, exception.cause)
    }

    private fun buildUseCase(repo: AuthRepository, local: AuthLocalDataSource): AuthUseCase {
        return AuthUseCase(repo, local)
    }

    private fun sampleRequest() = RegisterRequest(
        name = "Ana",
        surnames = "P",
        email = "ana@mail.com",
        phone = "600",
        password = "Secret123A",
        sex = "F",
        dateOfBirth = "1990-01-01",
        postCode = "28001",
        postAddress = "Calle 1",
        dni = "123",
        deviceType = "android",
        profilePicBytes = null,
        profilePicName = null,
    )

    private class FakeAuthRepository(
        private val loginResult: Result<LoginResponse> = Result.success(LoginResponse(1, "A", "a@a.com", "1", "M", "1991-01-01", null, "Dir", null, null, "at", "rt")),
        private val registerResult: Result<RegisterResponse> = Result.success(RegisterResponse("ok")),
        private val resetPasswordResult: Result<Unit> = Result.success(Unit),
        private val changePasswordResult: Result<Unit> = Result.success(Unit),
        private val logoutResult: Result<Unit> = Result.success(Unit),
    ) : AuthRepository {
        override suspend fun login(email: String, password: String) = loginResult
        override suspend fun register(data: RegisterRequest) = registerResult
        override suspend fun resetPassword(email: String) = resetPasswordResult
        override suspend fun changePassword(currentPassword: String, newPassword: String, userId: Int) = changePasswordResult
        override suspend fun logout() = logoutResult
    }

    private class FakeAuthLocalDataSource : AuthLocalDataSource {
        var clearCalled: Boolean = false
        override suspend fun getAccessToken(): String? = null
        override suspend fun getRefreshToken(): String? = null
        override fun accessTokenFlow(): Flow<String> = flowOf("")
        override fun userFlow(): Flow<User?> = flowOf(null)
        override suspend fun saveTokens(accessToken: String, refreshToken: String) = Unit
        override suspend fun clearTokens() = Unit
        override suspend fun saveUser(user: User) = Unit
        override suspend fun clear() { clearCalled = true }
    }
}
