package com.humanperformcenter.shared.presentation

import app.cash.turbine.test
import com.humanperformcenter.shared.data.model.auth.LoginResponse
import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.data.model.auth.RegisterResponse
import com.humanperformcenter.shared.domain.repository.AuthRepository
import com.humanperformcenter.shared.domain.storage.SessionStorage
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.presentation.ui.ChangePasswordState
import com.humanperformcenter.shared.presentation.ui.LoginState
import com.humanperformcenter.shared.presentation.ui.RegisterState
import com.humanperformcenter.shared.presentation.ui.ResetPasswordState
import com.humanperformcenter.shared.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthViewModelTest {

    private class FakeAuthRepository(
        private val loginResult: Result<LoginResponse> = Result.success(sampleLoginResponse()),
        private val registerResult: Result<RegisterResponse> = Result.success(RegisterResponse("ok")),
        private val resetPasswordResult: Result<Unit> = Result.success(Unit),
        private val changePasswordResult: Result<Unit> = Result.success(Unit),
        private val logoutResult: Result<Unit> = Result.success(Unit)
    ) : AuthRepository {
        override suspend fun login(email: String, password: String): Result<LoginResponse> = loginResult
        override suspend fun register(data: RegisterRequest): Result<RegisterResponse> = registerResult
        override suspend fun resetPassword(email: String): Result<Unit> = resetPasswordResult
        override suspend fun changePassword(currentPassword: String, newPassword: String, userId: Int): Result<Unit> =
            changePasswordResult
        override suspend fun logout(): Result<Unit> = logoutResult
    }

    private class FakeSessionStorage : SessionStorage {
        override suspend fun clearSession() = Unit
    }

    @Test
    fun login_whenSuccess_emitsLoadingThenSuccess() = runTest {
        val login = sampleLoginResponse()
        val viewModel = AuthViewModel(
            AuthUseCase(
                FakeAuthRepository(loginResult = Result.success(login)),
                FakeSessionStorage()
            )
        )

        viewModel.loginState.test {
            assertEquals(LoginState.Idle, awaitItem())
            viewModel.login("mail@test.com", "secret")
            assertEquals(LoginState.Loading, awaitItem())
            assertEquals(LoginState.Success(login), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_whenFailureWithoutMessage_emitsUnknownError() = runTest {
        val viewModel = AuthViewModel(
            AuthUseCase(
                FakeAuthRepository(loginResult = Result.failure(IllegalStateException())),
                FakeSessionStorage()
            )
        )

        viewModel.loginState.test {
            assertEquals(LoginState.Idle, awaitItem())
            viewModel.login("mail@test.com", "secret")
            assertEquals(LoginState.Loading, awaitItem())
            assertEquals(LoginState.Error("Error desconocido"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun register_whenInvalidData_emitsValidationErrors() = runTest {
        val viewModel = AuthViewModel(AuthUseCase(FakeAuthRepository(), FakeSessionStorage()))

        viewModel.register(
            sampleRegisterRequest().copy(
                email = "invalid-email",
                password = "123"
            )
        )

        val state = viewModel.registerState.value
        assertTrue(state is RegisterState.ValidationErrors)
        assertTrue(state.fieldErrors.isNotEmpty())
    }

    @Test
    fun register_whenSuccess_emitsLoadingThenSuccess() = runTest {
        val response = RegisterResponse("Creado")
        val viewModel = AuthViewModel(
            AuthUseCase(
                FakeAuthRepository(registerResult = Result.success(response)),
                FakeSessionStorage()
            )
        )

        viewModel.registerState.test {
            assertEquals(RegisterState.Idle, awaitItem())
            viewModel.register(sampleRegisterRequest())
            assertEquals(RegisterState.Loading, awaitItem())
            assertEquals(RegisterState.Success(response), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun resetPassword_and_changePassword_and_resets_updateStateAsExpected() = runTest {
        val viewModel = AuthViewModel(
            AuthUseCase(
                FakeAuthRepository(
                    resetPasswordResult = Result.success(Unit),
                    changePasswordResult = Result.success(Unit)
                ),
                FakeSessionStorage()
            )
        )

        viewModel.isResettingPassword.test {
            assertEquals(ResetPasswordState.Idle, awaitItem())
            viewModel.resetPassword("mail@test.com")
            assertEquals(ResetPasswordState.Loading, awaitItem())
            assertEquals(ResetPasswordState.Success("Contraseña restablecida exitosamente"), awaitItem())
            viewModel.resetResettingPasswordState()
            assertEquals(ResetPasswordState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.isChangingPassword.test {
            assertEquals(ChangePasswordState.Idle, awaitItem())
            viewModel.changePassword("Oldpass1", "Newpass1", "Newpass1", 1)
            assertEquals(ChangePasswordState.Loading, awaitItem())
            assertEquals(ChangePasswordState.Success("Contraseña cambiada exitosamente"), awaitItem())
            viewModel.resetChangePasswordState()
            assertEquals(ChangePasswordState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.login("mail@test.com", "secret")
        viewModel.register(sampleRegisterRequest())
        viewModel.resetStates()
        assertEquals(LoginState.Idle, viewModel.loginState.value)
        assertEquals(RegisterState.Idle, viewModel.registerState.value)
    }

    private companion object {
        fun sampleLoginResponse() = LoginResponse(
            id = 1,
            fullName = "Test User",
            email = "mail@test.com",
            phone = "600000000",
            sex = "M",
            dateOfBirth = "1990-01-01",
            postcode = 28001,
            postAddress = "Street 1",
            dni = "12345678A",
            profilePictureName = null,
            accessToken = "access",
            refreshToken = "refresh"
        )

        fun sampleRegisterRequest() = RegisterRequest(
            name = "Juan",
            surnames = "Pérez",
            email = "juan@test.com",
            phone = "600000000",
            password = "Password1",
            sex = "M",
            dateOfBirth = "1990-01-01",
            postCode = "28001",
            postAddress = "Street 1",
            dni = "12345678A",
            deviceType = "android",
            profilePicBytes = null,
            profilePicName = null
        )
    }
}
