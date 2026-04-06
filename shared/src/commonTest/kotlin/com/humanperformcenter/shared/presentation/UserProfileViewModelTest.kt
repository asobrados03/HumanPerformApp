package com.humanperformcenter.shared.presentation

import app.cash.turbine.test
import com.humanperformcenter.shared.data.local.UserProfileLocalDataSource
import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.repository.UserProfileRepository
import com.humanperformcenter.shared.domain.usecase.UserProfileUseCase
import com.humanperformcenter.shared.presentation.ui.DeleteProfilePicState
import com.humanperformcenter.shared.presentation.ui.UpdateState
import com.humanperformcenter.shared.presentation.viewmodel.UserProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserProfileViewModelTest {

    private val mainDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(mainDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─────────────────────────────────────────────────────────────
    // Fakes
    // ─────────────────────────────────────────────────────────────

    private class FakeUserProfileRepository(
        initialUsers: Map<Int, User> = mapOf(1 to sampleUser(1, "Fetched")),
        private val failUpdateWithMessage: String? = null,
        private val failGetWithMessage: String? = null,
        private val failDeletePicWithMessage: String? = null
    ) : UserProfileRepository {
        private val usersById = initialUsers.toMutableMap()

        override suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User> {
            failUpdateWithMessage?.let { return Result.failure(IllegalStateException(it)) }
            usersById[user.id] = user
            return Result.success(user)
        }

        override suspend fun getUserById(id: Int): Result<User> {
            failGetWithMessage?.let { return Result.failure(IllegalStateException(it)) }
            return usersById[id]?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("Usuario no encontrado"))
        }

        override suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit> {
            failDeletePicWithMessage?.let { return Result.failure(IllegalStateException(it)) }
            val userId = usersById.values.firstOrNull { it.email == req.email }?.id
            if (userId != null) {
                usersById[userId] = usersById.getValue(userId).copy(profilePictureName = null)
            }
            return Result.success(Unit)
        }
    }


    private class FakeUserProfileLocalDataSource : UserProfileLocalDataSource {
        private var currentUser: User? = null
        var saveUserCalls: Int = 0
            private set

        override suspend fun saveUser(user: User) {
            currentUser = user
            saveUserCalls += 1
        }

        override suspend fun getUser(): User? = currentUser

        override suspend fun clearUser() {
            currentUser = null
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────

    private data class TestContext(
        val viewModel: UserProfileViewModel,
        val localDataSource: FakeUserProfileLocalDataSource
    )

    private fun buildContext(repository: FakeUserProfileRepository = FakeUserProfileRepository()): TestContext {
        val localDataSource = FakeUserProfileLocalDataSource()
        val viewModel = UserProfileViewModel(UserProfileUseCase(repository), localDataSource)
        return TestContext(viewModel, localDataSource)
    }

    // ─────────────────────────────────────────────────────────────
    // Update user
    // ─────────────────────────────────────────────────────────────

    @Test
    fun updateUser_when_success_emits_loading_then_success_updates_current_user_and_persists_locally() = runTest {
        // Arrange
        val context = buildContext(
        // Act
            FakeUserProfileRepository(initialUsers = mapOf(1 to sampleUser(1, "Old")))
        )
        val viewModel = context.viewModel
        val currentUser = MutableStateFlow<User?>(sampleUser(1, "Old"))

        viewModel.updateState.test {
        // Assert
            assertEquals(UpdateState.Idle, awaitItem())
            viewModel.updateUser(sampleUser(1, "New"), null, currentUser)
            assertEquals(UpdateState.Loading, awaitItem())
            assertEquals(UpdateState.Success(sampleUser(1, "New")), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(sampleUser(1, "New"), currentUser.value)
        assertEquals(sampleUser(1, "New"), context.localDataSource.getUser())
        assertEquals(1, context.localDataSource.saveUserCalls)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateUser_when_success_with_new_profile_picture_name_updates_state_for_ios_ui() = runTest {
        // Arrange
        val context = buildContext(
            FakeUserProfileRepository(
                initialUsers = mapOf(1 to sampleUser(1, "Old").copy(profilePictureName = "old.jpg"))
            )
        )
        val currentUser = MutableStateFlow<User?>(sampleUser(1, "Old").copy(profilePictureName = "old.jpg"))
        val updatedUser = sampleUser(1, "Old").copy(profilePictureName = "new-ios-picture.jpg")

        // Act
        context.viewModel.updateUser(updatedUser, null, currentUser)
        advanceUntilIdle()

        // Assert
        assertEquals(UpdateState.Success(updatedUser), context.viewModel.updateState.value)
        assertEquals("new-ios-picture.jpg", currentUser.value?.profilePictureName)
        assertEquals("new-ios-picture.jpg", context.localDataSource.getUser()?.profilePictureName)
    }

    @Test
    fun updateUser_when_invalid_user_data_emits_validationerrors() = runTest {
        // Arrange
        val viewModel = buildContext().viewModel

        // Act
        viewModel.updateUser(sampleUser(1, ""), null, MutableStateFlow(null))

        // Assert
        assertTrue(viewModel.updateState.value is UpdateState.ValidationErrors)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateUser_when_repository_fails_emits_error() = runTest {
        // Arrange
        val viewModel = buildContext(
        // Act
            FakeUserProfileRepository(failUpdateWithMessage = "fail")
        ).viewModel

        viewModel.updateUser(sampleUser(1, "New"), null, MutableStateFlow(null))
        advanceUntilIdle()

        // Assert
        assertEquals(UpdateState.Error("fail"), viewModel.updateState.value)
    }

    @Test
    fun clearUpdateState_after_update_flow_restores_idle() = runTest {
        // Arrange
        val viewModel = buildContext().viewModel

        // Act
        viewModel.clearUpdateState()

        // Assert
        assertEquals(UpdateState.Idle, viewModel.updateState.value)
    }

    // ─────────────────────────────────────────────────────────────
    // Profile fetch and picture delete
    // ─────────────────────────────────────────────────────────────

    @Test
    fun fetchUserProfile_when_currentUser_is_null_returns_safely() = runTest {
        // Arrange
        val viewModel = buildContext().viewModel

        // Act
        viewModel.fetchUserProfile(MutableStateFlow(null))

        // Assert
        assertTrue(viewModel.updateState.value is UpdateState.Idle)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteProfilePic_when_repository_fails_emits_error_and_can_be_reset() = runTest {
        // Arrange
        val viewModel = buildContext(
        // Act
            FakeUserProfileRepository(failDeletePicWithMessage = "delete fail")
        ).viewModel

        viewModel.deleteProfilePic(sampleUser(1, "User"), MutableStateFlow(sampleUser(1, "User")))
        advanceUntilIdle()
        // Assert
        assertEquals(DeleteProfilePicState.Error("delete fail"), viewModel.deleteProfilePicState.value)

        viewModel.clearDeleteProfilePicState()
        assertEquals(DeleteProfilePicState.Idle, viewModel.deleteProfilePicState.value)
    }

    private companion object {
        fun sampleUser(id: Int, name: String) = User(
            id = id,
            fullName = name,
            email = "user$id@test.com",
            phone = "600000000",
            sex = "Male",
            dateOfBirth = "01/01/1990",
            postcode = 28001,
            postAddress = "Street 1",
            dni = "12345678Z",
            profilePictureName = "pic.jpg"
        )
    }
}
