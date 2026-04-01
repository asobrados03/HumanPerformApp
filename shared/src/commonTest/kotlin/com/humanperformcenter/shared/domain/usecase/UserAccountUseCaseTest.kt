package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.domain.repository.UserAccountRepository
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.koin.mp.KoinPlatform.stopKoin
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class UserAccountUseCaseTest : KoinTest {

    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun deleteUser_cuandoEmailValido_devuelveSuccess() = runTest {
        val useCase = buildUseCase(FakeRepo(Result.success(Unit)))
        assertTrue(useCase.deleteUser("ana@mail.com").isSuccess)
    }

    @Test
    fun deleteUser_cuandoEmailVacio_devuelveFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(Result.failure(IllegalArgumentException("email vacío"))))
        assertTrue(useCase.deleteUser("").isFailure)
    }

    @Test
    fun deleteUser_cuandoEmailConEspacios_devuelveFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(Result.failure(IllegalArgumentException("email inválido"))))
        assertTrue(useCase.deleteUser(" ").isFailure)
    }

    @Test
    fun deleteUser_cuandoRepositorioFalla_propagaFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(Result.failure(RuntimeException("server"))))
        assertTrue(useCase.deleteUser("x@mail.com").isFailure)
    }

    private fun buildUseCase(repo: UserAccountRepository): UserAccountUseCase {
        startKoin { modules(module { single<UserAccountRepository> { repo }; single { UserAccountUseCase(get()) } }) }
        return KoinPlatform.getKoin().get()
    }

    private class FakeRepo(private val result: Result<Unit>) : UserAccountRepository {
        override suspend fun deleteUser(email: String): Result<Unit> = result
    }
}
