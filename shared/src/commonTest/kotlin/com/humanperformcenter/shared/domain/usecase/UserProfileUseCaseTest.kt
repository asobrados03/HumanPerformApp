package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.repository.UserProfileRepository
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.koin.mp.KoinPlatform.stopKoin
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserProfileUseCaseTest : KoinTest {

    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun getUserById_whenIdIsValid_returnsUser() = runTest {
        val expected = User(2, "Ana", "ana@mail.com", "600", "F", "1990-01-01", 28001, "Calle 1", "123", null)
        val useCase = buildUseCase(FakeRepo(getResult = Result.success(expected)))
        assertEquals(expected, useCase.getUserById(2).getOrNull())
    }

    @Test
    fun updateUser_whenNoPhoto_returnsUpdatedUser() = runTest {
        val expected = User(2, "Ana P", "ana@mail.com", "600", "F", "1990-01-01", 28001, "Calle 1", "123", null)
        val useCase = buildUseCase(FakeRepo(updateResult = Result.success(expected)))
        assertEquals(expected, useCase.updateUser(expected, null).getOrNull())
    }

    @Test
    fun deleteProfilePicture_whenNameIsNull_returnsSuccess() = runTest {
        val useCase = buildUseCase(FakeRepo(deleteResult = Result.success(Unit)))
        assertTrue(useCase.deleteProfilePicture(DeleteProfilePicRequest("ana@mail.com", null)).isSuccess)
    }

    @Test
    fun getUserById_whenRepositoryFails_propagatesFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(getResult = Result.failure(RuntimeException("not found"))))
        assertTrue(useCase.getUserById(2).isFailure)
    }

    private fun buildUseCase(repo: UserProfileRepository): UserProfileUseCase {
        startKoin { modules(module { single<UserProfileRepository> { repo }; single { UserProfileUseCase(get()) } }) }
        return KoinPlatform.getKoin().get()
    }

    private class FakeRepo(
        private val updateResult: Result<User> = Result.failure(IllegalStateException("unused")),
        private val getResult: Result<User> = Result.failure(IllegalStateException("unused")),
        private val deleteResult: Result<Unit> = Result.success(Unit),
    ) : UserProfileRepository {
        override suspend fun updateUser(user: User, profilePicBytes: ByteArray?) = updateResult
        override suspend fun getUserById(id: Int) = getResult
        override suspend fun deleteProfilePic(req: DeleteProfilePicRequest) = deleteResult
    }
}
