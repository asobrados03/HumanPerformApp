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
        private val deleteResult: Result<Unit> = Result.success(Unit)
    ) : UserAccountRepository {
        override suspend fun deleteUser(email: String): Result<Unit> = deleteResult
    }

    private class FakeAuthRepository(
        private val logoutResult: Result<Unit> = Result.success(Unit)
    ) : AuthRepository {
        override suspend fun login(email: String, password: String): Result<LoginResponse> =
            Result.failure(UnsupportedOperationException())

        override suspend fun register(data: RegisterRequest): Result<RegisterResponse> =
            Result.failure(UnsupportedOperationException())

        override suspend fun resetPassword(email: String): Result<Unit> =
            Result.failure(UnsupportedOperationException())

        override suspend fun changePassword(currentPassword: String, newPassword: String, userId: Int): Result<Unit> =
            Result.failure(UnsupportedOperationException())

        override suspend fun logout(): Result<Unit> = logoutResult
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
        deleteResult: Result<Unit> = Result.success(Unit),
        logoutResult: Result<Unit> = Result.success(Unit)
    ): UserSessionViewModel {
        val localDataSource = FakeAuthLocalDataSource()
        return UserSessionViewModel(
            userAccountUseCase = UserAccountUseCase(FakeUserAccountRepository(deleteResult)),
            authUseCase = AuthUseCase(FakeAuthRepository(logoutResult), localDataSource),
            authLocalDataSource = localDataSource,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteUser_when_success_emits_success_and_can_be_reset() = runTest {
        val viewModel = buildViewModel(deleteResult = Result.success(Unit))

        viewModel.deleteUser("user@test.com")
        advanceTimeBy(1000)
        advanceUntilIdle()
        assertEquals(DeleteUserState.Success, viewModel.deleteState.value)

        viewModel.resetDeleteState()
        assertEquals(DeleteUserState.Idle, viewModel.deleteState.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteUser_when_not_found_emits_notfound() = runTest {
        val viewModel = buildViewModel(
            deleteResult = Result.failure(IllegalStateException("usuario no encontrado"))
        )

        viewModel.deleteUser("user@test.com")
        advanceUntilIdle()

        assertEquals(DeleteUserState.NotFound("user@test.com"), viewModel.deleteState.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteUser_when_generic_failure_emits_error() = runTest {
        val viewModel = buildViewModel(
            deleteResult = Result.failure(IllegalStateException("fallo"))
        )

        viewModel.deleteUser("user@test.com")
        advanceUntilIdle()

        assertEquals(DeleteUserState.Error("fallo"), viewModel.deleteState.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun logout_when_success_invokes_callback_and_updates_state() = runTest {
        val viewModel = buildViewModel(logoutResult = Result.success(Unit))
        var callbackCalled = false

        viewModel.logout { callbackCalled = true }
        advanceTimeBy(800)
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertEquals(false, viewModel.isLoggingOut.value)
    }

    @Test
    fun session_helpers_currentUserState_and_isLoggedInFlow_expose_expected_values() = runTest {
        val viewModel = buildViewModel()

        assertEquals(viewModel.userData.value, viewModel.currentUserState().value)
        assertEquals(false, viewModel.isLoggedInFlow.map { it }.first())
    }
}
