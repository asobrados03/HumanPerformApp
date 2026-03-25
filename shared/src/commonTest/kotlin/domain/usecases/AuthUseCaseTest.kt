package com.humanperformcenter.shared.domain.usecases

import com.humanperformcenter.shared.domain.repository.AuthRepository
import com.humanperformcenter.shared.domain.storage.SessionStorage
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.domain.usecase.validation.ChangePasswordException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthUseCaseTest {

    private class FakeAuthRepository(
        private val onChangePassword: (String, String, Int) -> Result<Unit>
    ) : AuthRepository {
        override suspend fun login(email: String, password: String) = error("Not used")
        override suspend fun register(data: com.humanperformcenter.shared.data.model.auth.RegisterRequest) = error("Not used")
        override suspend fun resetPassword(email: String) = error("Not used")
        override suspend fun logout() = Result.success(Unit)
        override suspend fun changePassword(currentPassword: String, newPassword: String, userId: Int): Result<Unit> =
            onChangePassword(currentPassword, newPassword, userId)
    }

    private class FakeSessionStorage : SessionStorage {
        override suspend fun clearSession() = Unit
    }

    @Test
    fun authUseCase_whenChangePasswordIsValid_returnsSuccess() = runBlocking {
        // Arrange
        val useCase = AuthUseCase(FakeAuthRepository { _, _, _ -> Result.success(Unit) }, FakeSessionStorage())

        // Act
        val result = useCase.changePassword("OldPass123", "NewPass123", "NewPass123", 5)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun authUseCase_whenNewPasswordIsBlank_returnsValidationError() = runBlocking {
        // Arrange
        val useCase = AuthUseCase(FakeAuthRepository { _, _, _ -> Result.success(Unit) }, FakeSessionStorage())

        // Act
        val result = useCase.changePassword("OldPass123", "", "", 5)

        // Assert
        assertTrue(result.isFailure)
        assertIs<ChangePasswordException.NewRequired>(result.exceptionOrNull())
    }

    @Test
    fun authUseCase_whenRepositoryFails_returnsRepoFailure() = runBlocking {
        // Arrange
        val useCase = AuthUseCase(
            FakeAuthRepository { _, _, _ -> Result.failure(IllegalStateException("boom")) },
            FakeSessionStorage()
        )

        // Act
        val result = useCase.changePassword("OldPass123", "NewPass123", "NewPass123", 5)

        // Assert
        assertTrue(result.isFailure)
        assertIs<ChangePasswordException.RepoFailure>(result.exceptionOrNull())
    }
}
