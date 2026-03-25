package com.humanperformcenter.shared.domain.usecases

import com.humanperformcenter.shared.domain.repository.UserAccountRepository
import com.humanperformcenter.shared.domain.usecase.UserAccountUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserAccountUseCaseTest {

    private class FakeUserAccountRepository(
        private val deleteUserBehavior: (String) -> Result<Unit>
    ) : UserAccountRepository {
        override suspend fun deleteUser(email: String): Result<Unit> = deleteUserBehavior(email)
    }

    @Test
    fun userAccountUseCase_whenDeleteUserSucceeds_returnsSuccess() = runBlocking {
        // Arrange
        val repository = FakeUserAccountRepository { Result.success(Unit) }
        val useCase = UserAccountUseCase(repository)

        // Act
        val result = useCase.deleteUser("user@example.com")

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun userAccountUseCase_whenEmailIsEmpty_propagatesRepositoryFailure() = runBlocking {
        // Arrange
        val expectedMessage = "Email vacío"
        val repository = FakeUserAccountRepository { email ->
            if (email.isBlank()) Result.failure(IllegalArgumentException(expectedMessage))
            else Result.success(Unit)
        }
        val useCase = UserAccountUseCase(repository)

        // Act
        val result = useCase.deleteUser("")

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedMessage, result.exceptionOrNull()?.message)
    }

    @Test
    fun userAccountUseCase_whenRepositoryFails_propagatesFailure() = runBlocking {
        // Arrange
        val expectedMessage = "Error del repositorio"
        val repository = FakeUserAccountRepository {
            Result.failure(RuntimeException(expectedMessage))
        }
        val useCase = UserAccountUseCase(repository)

        // Act
        val result = useCase.deleteUser("user@example.com")

        // Assert
        assertTrue(result.isFailure)
        assertEquals(expectedMessage, result.exceptionOrNull()?.message)
    }
}
