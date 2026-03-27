package com.humanperformcenter.shared.presentation

import app.cash.turbine.test
import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.repository.UserProfileRepository
import com.humanperformcenter.shared.domain.usecase.UserProfileUseCase
import com.humanperformcenter.shared.presentation.ui.DeleteProfilePicState
import com.humanperformcenter.shared.presentation.ui.UpdateState
import com.humanperformcenter.shared.presentation.viewmodel.UserProfileViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserProfileViewModelTest {

    private class FakeUserProfileRepository(
        private val updateResult: Result<User> = Result.success(sampleUser(1, "Updated")),
        private val getByIdResult: Result<User> = Result.success(sampleUser(1, "Fetched")),
        private val deletePicResult: Result<Unit> = Result.success(Unit)
    ) : UserProfileRepository {
        override suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User> = updateResult
        override suspend fun getUserById(id: Int): Result<User> = getByIdResult
        override suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit> = deletePicResult
    }

    @Test
    fun updateUser_validationSuccessAndFailure_plus_clearState() = runTest {
        val currentUser = MutableStateFlow<User?>(sampleUser(1, "Old"))
        val successVm = UserProfileViewModel(
            UserProfileUseCase(FakeUserProfileRepository(updateResult = Result.success(sampleUser(1, "New"))))
        )

        successVm.updateState.test {
            assertEquals(UpdateState.Idle, awaitItem())
            successVm.updateUser(sampleUser(1, "New"), null, currentUser)
            assertEquals(UpdateState.Loading, awaitItem())
            assertEquals(UpdateState.Success(sampleUser(1, "New")), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(sampleUser(1, "New"), currentUser.value)

        successVm.clearUpdateState()
        assertEquals(UpdateState.Idle, successVm.updateState.value)

        val validationVm = UserProfileViewModel(UserProfileUseCase(FakeUserProfileRepository()))
        validationVm.updateUser(sampleUser(1, ""), null, MutableStateFlow(null))
        assertTrue(validationVm.updateState.value is UpdateState.ValidationErrors)

        val errorVm = UserProfileViewModel(
            UserProfileUseCase(FakeUserProfileRepository(updateResult = Result.failure(IllegalStateException("fail"))))
        )
        errorVm.updateUser(sampleUser(1, "New"), null, MutableStateFlow(null))
        assertEquals(UpdateState.Error("fail"), errorVm.updateState.value)
    }

    @Test
    fun fetchUserProfile_and_deleteProfilePic_cover_safe_paths() = runTest {
        val vm = UserProfileViewModel(UserProfileUseCase(FakeUserProfileRepository()))

        // currentUser nulo: return temprano, no crash
        vm.fetchUserProfile(MutableStateFlow(null))

        // deleteProfilePic en fallo evita tocar SecureStorage
        val deleteErrorVm = UserProfileViewModel(
            UserProfileUseCase(
                FakeUserProfileRepository(deletePicResult = Result.failure(IllegalStateException("delete fail")))
            )
        )

        deleteErrorVm.deleteProfilePic(sampleUser(1, "User"), MutableStateFlow(sampleUser(1, "User")))
        assertEquals(DeleteProfilePicState.Error("delete fail"), deleteErrorVm.deleteProfilePicState.value)

        deleteErrorVm.clearDeleteProfilePicState()
        assertEquals(DeleteProfilePicState.Idle, deleteErrorVm.deleteProfilePicState.value)
    }

    private companion object {
        fun sampleUser(id: Int, name: String) = User(
            id = id,
            fullName = name,
            email = "user$id@test.com",
            phone = "600000000",
            sex = "M",
            dateOfBirth = "1990-01-01",
            postcode = 28001,
            postAddress = "Street 1",
            dni = "12345678A",
            profilePictureName = "pic.jpg"
        )
    }
}
