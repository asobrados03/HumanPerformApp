package com.humanperformcenter.shared.presentation

import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.model.auth.LoginResponse
import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.data.model.auth.RegisterResponse
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.repository.AuthRepository
import com.humanperformcenter.shared.domain.repository.UserAccountRepository
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.domain.usecase.UserAccountUseCase
import com.humanperformcenter.shared.presentation.ui.DeleteUserState
import com.humanperformcenter.shared.presentation.viewmodel.UserSessionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserSessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeUserAccountRepository(
        existingEmails: Set<String> = setOf("user@test.com"),
        private val forceDeleteFailureMessage: String? = null
    ) : UserAccountRepository {
        private val users = existingEmails.toMutableSet()

        override suspend fun deleteUser(email: String): Result<Unit> {
            forceDeleteFailureMessage?.let { return Result.failure(IllegalStateException(it)) }
            if (!users.remove(email)) {
                return Result.failure(IllegalStateException("usuario no encontrado"))
            }
            return Result.success(Unit)
        }
    }

    private class FakeAuthRepository(
        private val forceLogoutFailureMessage: String? = null
    ) : AuthRepository {
        override suspend fun login(email: String, password: String): Result<LoginResponse> =
            Result.failure(UnsupportedOperationException())

        override suspend fun register(data: RegisterRequest): Result<RegisterResponse> =
            Result.failure(UnsupportedOperationException())

        override suspend fun resetPassword(email: String): Result<Unit> =
            Result.failure(UnsupportedOperationException())

        override suspend fun changePassword(currentPassword: String, newPassword: String, userId: Int): Result<Unit> =
            Result.failure(UnsupportedOperationException())

        override suspend fun logout(): Result<Unit> {
            forceLogoutFailureMessage?.let { return Result.failure(IllegalStateException(it)) }
            return Result.success(Unit)
        }
    }

    private class FakeAuthLocalDataSource : AuthLocalDataSource {
        private val token = MutableStateFlow("")
        private val user = MutableStateFlow<User?>(null)

        override suspend fun getAccessToken(): String? = token.value.ifBlank { null }
        override suspend fun getRefreshToken(): String? = null
        override fun accessTokenFlow(): Flow<String> = token
        override fun userFlow(): Flow<User?> = user
        override suspend fun saveTokens(accessToken: String, refreshToken: String) {
            token.value = accessToken
        }

        override suspend fun saveTokensAndUser(accessToken: String, refreshToken: String, user: User) {
            token.value = accessToken
            this.user.value = user
        }

        override suspend fun clearTokens() {
            token.value = ""
        }

        override suspend fun saveUser(user: User) {
            this.user.value = user
        }

        override suspend fun clear() {
            token.value = ""
            user.value = null
        }
    }

    private fun buildViewModel(
        existingEmails: Set<String> = setOf("user@test.com"),
        deleteFailureMessage: String? = null,
        logoutFailureMessage: String? = null
    ): UserSessionViewModel {
        val localDataSource = FakeAuthLocalDataSource()
        return UserSessionViewModel(
            userAccountUseCase = UserAccountUseCase(
                FakeUserAccountRepository(
                    existingEmails = existingEmails,
                    forceDeleteFailureMessage = deleteFailureMessage
                )
            ),
            authUseCase = AuthUseCase(
                FakeAuthRepository(forceLogoutFailureMessage = logoutFailureMessage),
                localDataSource
            ),
            authLocalDataSource = localDataSource,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteUser_when_success_emits_success_and_can_be_reset() = runTest {
        // Arrange
        val viewModel = buildViewModel(existingEmails = setOf("user@test.com"))

        // Act
        viewModel.deleteUser("user@test.com")
        advanceTimeBy(1000)
        advanceUntilIdle()
        // Assert
        assertEquals(DeleteUserState.Success, viewModel.deleteState.value)

        viewModel.resetDeleteState()
        assertEquals(DeleteUserState.Idle, viewModel.deleteState.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteUser_when_not_found_emits_notfound() = runTest {
        // Arrange
        val viewModel = buildViewModel(
        // Act
            existingEmails = emptySet()
        )

        viewModel.deleteUser("user@test.com")
        advanceUntilIdle()

        // Assert
        assertEquals(DeleteUserState.NotFound("user@test.com"), viewModel.deleteState.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteUser_when_generic_failure_emits_error() = runTest {
        // Arrange
        val viewModel = buildViewModel(
        // Act
            deleteFailureMessage = "fallo"
        )

        viewModel.deleteUser("user@test.com")
        advanceUntilIdle()

        // Assert
        assertEquals(DeleteUserState.Error("fallo"), viewModel.deleteState.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun logout_when_success_invokes_callback_and_updates_state() = runTest {
        // Arrange
        val viewModel = buildViewModel()
        var callbackCalled = false

        // Act
        viewModel.logout { callbackCalled = true }
        advanceTimeBy(800)
        advanceUntilIdle()

        // Assert
        assertTrue(callbackCalled)
        assertEquals(false, viewModel.isLoggingOut.value)
    }

    @Test
    fun session_helpers_currentUserState_and_isLoggedInFlow_expose_expected_values() = runTest {
        // Arrange
        val viewModel = buildViewModel()

        // Assert
        assertEquals(viewModel.userData.value, viewModel.currentUserState().value)
        assertEquals(false, viewModel.isLoggedInFlow.map { it }.first())
    }
}
