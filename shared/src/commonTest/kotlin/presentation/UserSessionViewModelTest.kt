package com.humanperformcenter.shared.presentation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.humanperformcenter.shared.data.model.auth.LoginResponse
import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.data.model.auth.RegisterResponse
import com.humanperformcenter.shared.domain.repository.AuthRepository
import com.humanperformcenter.shared.domain.repository.UserAccountRepository
import com.humanperformcenter.shared.domain.storage.SecureStorage
import com.humanperformcenter.shared.domain.storage.SessionStorage
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.domain.usecase.UserAccountUseCase
import com.humanperformcenter.shared.presentation.ui.DeleteUserState
import com.humanperformcenter.shared.presentation.viewmodel.UserSessionViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserSessionViewModelTest {

    private class InMemoryPreferencesDataStore : DataStore<Preferences> {
        private val state = MutableStateFlow<Preferences>(emptyPreferences())
        override val data: Flow<Preferences> = state

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val newValue = transform(state.value)
            state.value = newValue
            return newValue
        }
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

    private class FakeSessionStorage : SessionStorage {
        override suspend fun clearSession() = Unit
    }

    private fun buildViewModel(
        deleteResult: Result<Unit> = Result.success(Unit),
        logoutResult: Result<Unit> = Result.success(Unit)
    ): UserSessionViewModel {
        SecureStorage.initialize(InMemoryPreferencesDataStore())
        return UserSessionViewModel(
            userAccountUseCase = UserAccountUseCase(FakeUserAccountRepository(deleteResult)),
            authUseCase = AuthUseCase(FakeAuthRepository(logoutResult), FakeSessionStorage())
        )
    }

    @Test
    fun deleteuser_when_success_emits_success_and_can_be_reset() = runTest {
        val viewModel = buildViewModel(deleteResult = Result.success(Unit))

        viewModel.deleteUser("user@test.com")
        assertEquals(DeleteUserState.Success, viewModel.deleteState.value)

        viewModel.resetDeleteState()
        assertEquals(DeleteUserState.Idle, viewModel.deleteState.value)
    }

    @Test
    fun deleteuser_when_not_found_emits_notfound() = runTest {
        val viewModel = buildViewModel(
            deleteResult = Result.failure(IllegalStateException("usuario no encontrado"))
        )

        viewModel.deleteUser("user@test.com")

        assertEquals(DeleteUserState.NotFound("user@test.com"), viewModel.deleteState.value)
    }

    @Test
    fun deleteuser_when_generic_failure_emits_error() = runTest {
        val viewModel = buildViewModel(
            deleteResult = Result.failure(IllegalStateException("fallo"))
        )

        viewModel.deleteUser("user@test.com")

        assertEquals(DeleteUserState.Error("fallo"), viewModel.deleteState.value)
    }

    @Test
    fun logout_when_success_invokes_callback_and_updates_state() = runTest {
        val viewModel = buildViewModel(logoutResult = Result.success(Unit))
        var callbackCalled = false

        viewModel.logout { callbackCalled = true }

        assertTrue(callbackCalled)
        assertEquals(false, viewModel.isLoggingOut.value)
    }

    @Test
    fun session_helpers_currentuserstate_and_isloggedinflow_expose_expected_values() = runTest {
        val viewModel = buildViewModel()

        assertEquals(viewModel.userData.value, viewModel.currentUserState().value)
        assertEquals(false, viewModel.isLoggedInFlow.first())
    }
}
