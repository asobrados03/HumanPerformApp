package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.local.UserProfileLocalDataSource
import com.humanperformcenter.shared.data.model.user.DeleteProfilePicRequest
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.remote.UserProfileRemoteDataSource
import com.humanperformcenter.shared.domain.DomainException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserProfileRepositoryImplTest {

    @Test
    fun updateuser_when_success_persists_user_in_local_datasource() = runTest {
        val updatedUser = sampleUser().copy(fullName = "Nuevo Nombre")
        val remote = FakeUserProfileRemoteDataSource(updateResult = Result.success(updatedUser))
        val local = FakeUserProfileLocalDataSource()
        val repository = UserProfileRepositoryImpl(remote, local)

        val result = repository.updateUser(sampleUser(), profilePicBytes = byteArrayOf(1, 2, 3))

        assertTrue(result.isSuccess)
        assertEquals(updatedUser, result.getOrNull())
        assertEquals(updatedUser, local.savedUser)
    }

    @Test
    fun updateuser_when_backend_error_maps_to_not_found_and_does_not_persist() = runTest {
        val remote = FakeUserProfileRemoteDataSource(
            updateResult = Result.failure(IllegalStateException("HTTP 404 User not found")),
        )
        val local = FakeUserProfileLocalDataSource()
        val repository = UserProfileRepositoryImpl(remote, local)

        val result = repository.updateUser(sampleUser(), profilePicBytes = null)

        assertTrue(result.isFailure)
        assertIs<DomainException.NotFound>(result.exceptionOrNull())
        assertNull(local.savedUser)
    }

    @Test
    fun getuserbyid_when_network_exception_maps_to_domain_network() = runTest {
        val remote = FakeUserProfileRemoteDataSource(
            getByIdResult = Result.failure(IOException("No internet")),
        )
        val repository = UserProfileRepositoryImpl(remote, FakeUserProfileLocalDataSource())

        val result = repository.getUserById(10)

        assertTrue(result.isFailure)
        assertIs<DomainException.Network>(result.exceptionOrNull())
    }

    private class FakeUserProfileRemoteDataSource(
        private val updateResult: Result<User> = Result.success(sampleUser()),
        private val getByIdResult: Result<User> = Result.success(sampleUser()),
        private val deletePicResult: Result<Unit> = Result.success(Unit),
    ) : UserProfileRemoteDataSource {
        override suspend fun updateUser(user: User, profilePicBytes: ByteArray?): Result<User> = updateResult
        override suspend fun getUserById(id: Int): Result<User> = getByIdResult
        override suspend fun deleteProfilePic(req: DeleteProfilePicRequest): Result<Unit> = deletePicResult
    }

    private class FakeUserProfileLocalDataSource : UserProfileLocalDataSource {
        var savedUser: User? = null

        override suspend fun saveUser(user: User) {
            savedUser = user
        }

        override suspend fun getUser(): User? = savedUser

        override suspend fun clearUser() {
            savedUser = null
        }
    }

    private companion object {
        fun sampleUser() = User(
            id = 10,
            fullName = "Carlos Ruiz",
            email = "carlos@test.com",
            phone = "611111111",
            sex = "M",
            dateOfBirth = "1991-07-20",
            postcode = 28080,
            postAddress = "Gran Vía 2",
            dni = "99999999Z",
            profilePictureName = "carlos.png",
        )
    }
}
