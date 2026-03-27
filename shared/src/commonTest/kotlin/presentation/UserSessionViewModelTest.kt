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

    @Test
    fun deleteUser_resetDeleteState_logout_and_currentUserState_work() = runTest {
        SecureStorage.initialize(InMemoryPreferencesDataStore())
        val vm = UserSessionViewModel(
            userAccountUseCase = UserAccountUseCase(FakeUserAccountRepository(Result.success(Unit))),
            authUseCase = AuthUseCase(FakeAuthRepository(Result.success(Unit)), FakeSessionStorage())
        )

        vm.deleteUser("user@test.com")
        assertEquals(DeleteUserState.Success, vm.deleteState.value)

        vm.resetDeleteState()
        assertEquals(DeleteUserState.Idle, vm.deleteState.value)

        var callbackCalled = false
        vm.logout { callbackCalled = true }
        assertTrue(callbackCalled)
        assertEquals(false, vm.isLoggingOut.value)

        assertEquals(vm.userData.value, vm.currentUserState().value)
        assertEquals(false, vm.isLoggedInFlow.first())
    }

    @Test
    fun deleteUser_handles_notFound_and_generic_errors() = runTest {
        SecureStorage.initialize(InMemoryPreferencesDataStore())

        val notFoundVm = UserSessionViewModel(
            userAccountUseCase = UserAccountUseCase(
                FakeUserAccountRepository(Result.failure(IllegalStateException("usuario no encontrado")))
            ),
            authUseCase = AuthUseCase(FakeAuthRepository(), FakeSessionStorage())
        )
        notFoundVm.deleteUser("user@test.com")
        assertEquals(DeleteUserState.NotFound("user@test.com"), notFoundVm.deleteState.value)

        val errorVm = UserSessionViewModel(
            userAccountUseCase = UserAccountUseCase(
                FakeUserAccountRepository(Result.failure(IllegalStateException("fallo")))
            ),
            authUseCase = AuthUseCase(FakeAuthRepository(), FakeSessionStorage())
        )
        errorVm.deleteUser("user@test.com")
        assertEquals(DeleteUserState.Error("fallo"), errorVm.deleteState.value)
    }
}
