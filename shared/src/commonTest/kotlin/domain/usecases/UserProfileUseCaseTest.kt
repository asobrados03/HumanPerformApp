package com.humanperformcenter.shared.domain.usecases

import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.repository.UserProfileRepository
import com.humanperformcenter.shared.domain.usecase.UserProfileUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserProfileUseCaseTest {
    private class FakeUserProfileRepository(
        private val getUserResult: Result<User>
    ) : UserProfileRepository {
        override suspend fun updateUser(user: User, profilePicBytes: ByteArray?) = error("Not used")
        override suspend fun getUserById(id: Int): Result<User> = getUserResult
        override suspend fun deleteProfilePic(req: com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest) = error("Not used")
    }

    @Test
    fun userProfileUseCase_whenRepositoryReturnsUser_returnsSuccess() = runBlocking {
        // Arrange
        val user = User(1, "Name", "mail@test.com", "123", "M", "1990-01-01", null, "Street", null, null)
        val useCase = UserProfileUseCase(FakeUserProfileRepository(Result.success(user)))

        // Act
        val result = useCase.getUserById(1)

        // Assert
        assertEquals(1, result.getOrNull()?.id)
    }

    @Test
    fun userProfileUseCase_whenRepositoryReturnsEmptyName_keepsValue() = runBlocking {
        // Arrange
        val user = User(1, "", "mail@test.com", "123", "M", "1990-01-01", null, "", null, null)
        val useCase = UserProfileUseCase(FakeUserProfileRepository(Result.success(user)))

        // Act
        val result = useCase.getUserById(1)

        // Assert
        assertEquals("", result.getOrNull()?.fullName)
    }

    @Test
    fun userProfileUseCase_whenRepositoryFails_propagatesFailure() = runBlocking {
        // Arrange
        val useCase = UserProfileUseCase(FakeUserProfileRepository(Result.failure(IllegalStateException("boom"))))

        // Act
        val result = useCase.getUserById(1)

        // Assert
        assertTrue(result.isFailure)
    }
}
