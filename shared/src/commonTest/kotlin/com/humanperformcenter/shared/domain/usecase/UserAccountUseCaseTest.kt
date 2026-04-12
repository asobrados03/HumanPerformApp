package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.domain.repository.UserAccountRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class UserAccountUseCaseTest {

    @Test
    fun deleteUser_whenEmailIsValid_returnsSuccess() = runTest {
        val useCase = buildUseCase(FakeRepo(Result.success(Unit)))
        assertTrue(useCase.deleteUser("ana@mail.com").isSuccess)
    }

    @Test
    fun deleteUser_whenEmailIsEmpty_returnsFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(Result.failure(IllegalArgumentException("email vacío"))))
        assertTrue(useCase.deleteUser("").isFailure)
    }

    @Test
    fun deleteUser_whenEmailHasSpaces_returnsFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(Result.failure(IllegalArgumentException("email inválido"))))
        assertTrue(useCase.deleteUser(" ").isFailure)
    }

    @Test
    fun deleteUser_whenRepositoryFails_propagatesFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(Result.failure(RuntimeException("server"))))
        assertTrue(useCase.deleteUser("x@mail.com").isFailure)
    }

    private fun buildUseCase(repo: UserAccountRepository): UserAccountUseCase {
        return UserAccountUseCase(repo)
    }

    private class FakeRepo(private val result: Result<Unit>) : UserAccountRepository {
        override suspend fun deleteUser(email: String): Result<Unit> = result
    }
}
