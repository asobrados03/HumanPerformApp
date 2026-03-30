package com.humanperformcenter.shared.presentation

import app.cash.turbine.test
import com.humanperformcenter.shared.data.model.auth.LoginResponse
import com.humanperformcenter.shared.data.model.auth.RegisterRequest
import com.humanperformcenter.shared.data.model.auth.RegisterResponse
import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.repository.AuthRepository
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.presentation.ui.ChangePasswordState
import com.humanperformcenter.shared.presentation.ui.LoginState
import com.humanperformcenter.shared.presentation.ui.RegisterState
import com.humanperformcenter.shared.presentation.ui.ResetPasswordState
import com.humanperformcenter.shared.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.koin.mp.KoinPlatform.stopKoin
import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest : KoinTest {
    // Contrato unificado para fallbacks sin mensaje:
    // - Login usa fallback genérico: "Error desconocido".
    // - Register / ResetPassword / ChangePassword usan fallback específico por acción
    //   definido en AuthViewModel para mantener mensajes más orientados al contexto.

    // ─────────────────────────────────────────────────────────────
    // Fakes
    // ─────────────────────────────────────────────────────────────

    private class FakeAuthRepository(
        private val loginResult: Result<LoginResponse> = Result.success(sampleLoginResponse()),
        private val registerResult: Result<RegisterResponse> = Result.success(RegisterResponse("ok")),
        private val resetPasswordResult: Result<Unit> = Result.success(Unit),
        private val changePasswordResult: Result<Unit> = Result.success(Unit),
        private val logoutResult: Result<Unit> = Result.success(Unit)
    ) : AuthRepository {

        var loginCallCount = 0
            private set
        var registerCallCount = 0
            private set

        override suspend fun login(email: String, password: String): Result<LoginResponse> {
            loginCallCount++
            return loginResult
        }

        override suspend fun register(data: RegisterRequest): Result<RegisterResponse> {
            registerCallCount++
            return registerResult
        }

        override suspend fun resetPassword(email: String): Result<Unit> = resetPasswordResult
        override suspend fun changePassword(
            currentPassword: String,
            newPassword: String,
            userId: Int
        ): Result<Unit> = changePasswordResult
        override suspend fun logout(): Result<Unit> = logoutResult
    }

    private class FakeAuthLocalDataSource : AuthLocalDataSource {
        override suspend fun getAccessToken(): String? = null
        override suspend fun getRefreshToken(): String? = null
        override fun accessTokenFlow(): Flow<String> = MutableStateFlow("")
        override fun userFlow(): Flow<User?> = MutableStateFlow(null)
        override suspend fun saveTokens(accessToken: String, refreshToken: String) = Unit
        override suspend fun clearTokens() = Unit
        override suspend fun saveUser(user: User) = Unit
        override suspend fun clear() = Unit
    }

    // ─────────────────────────────────────────────────────────────
    // Koin setup
    // ─────────────────────────────────────────────────────────────

    private val testDispatcher = StandardTestDispatcher()

    private fun testModule(repository: FakeAuthRepository = FakeAuthRepository()) = module {
        single<AuthRepository> { repository }
        single<AuthLocalDataSource> { FakeAuthLocalDataSource() }
        single { AuthUseCase(get(), get()) }
        viewModel { AuthViewModel(get()) }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    // ─────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────

    /**
     * Arranca un contenedor Koin aislado para el test y devuelve el ViewModel
     * resuelto por el grafo, igual que lo haría la app real.
     */
    private fun buildViewModel(repository: FakeAuthRepository = FakeAuthRepository()): AuthViewModel {
        startKoin { modules(testModule(repository)) }
        return KoinPlatform.getKoin().get()
    }

    // ─────────────────────────────────────────────────────────────
    // Login
    // ─────────────────────────────────────────────────────────────

    @Test
    fun login_when_success_emits_loading_then_success() = runTest {
        val login = sampleLoginResponse()
        val viewModel = buildViewModel(
            FakeAuthRepository(loginResult = Result.success(login))
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
    fun login_when_failure_with_message_emits_error_with_that_message() = runTest {
        val viewModel = buildViewModel(
            FakeAuthRepository(loginResult = Result.failure(IllegalStateException("Credenciales inválidas")))
        )

        viewModel.loginState.test {
            assertEquals(LoginState.Idle, awaitItem())
            viewModel.login("mail@test.com", "wrong")
            assertEquals(LoginState.Loading, awaitItem())
            assertEquals(LoginState.Error("Credenciales inválidas"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun login_when_failure_without_message_emits_unknown_error() = runTest {
        val viewModel = buildViewModel(
            FakeAuthRepository(loginResult = Result.failure(IllegalStateException()))
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
    fun resetstates_after_login_success_restores_loginstate_to_idle() = runTest {
        val viewModel = buildViewModel()

        viewModel.login("mail@test.com", "secret")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.resetStates()

        assertEquals(LoginState.Idle, viewModel.loginState.value)
    }

    // ─────────────────────────────────────────────────────────────
    // Register
    // ─────────────────────────────────────────────────────────────

    @Test
    fun register_when_success_emits_loading_then_success() = runTest {
        val response = RegisterResponse("Creado")
        val repository = FakeAuthRepository(registerResult = Result.success(response))
        val viewModel = buildViewModel(repository)

        viewModel.registerState.test {
            assertEquals(RegisterState.Idle, awaitItem())
            viewModel.register(sampleRegisterRequest())
            assertEquals(RegisterState.Loading, awaitItem())
            assertEquals(RegisterState.Success(response), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, repository.registerCallCount)
    }

    @Test
    fun register_when_failure_with_message_emits_error_with_that_message() = runTest {
        val repository = FakeAuthRepository(
            registerResult = Result.failure(RuntimeException("Email ya registrado"))
        )
        val viewModel = buildViewModel(repository)

        viewModel.registerState.test {
            assertEquals(RegisterState.Idle, awaitItem())
            viewModel.register(sampleRegisterRequest())
            assertEquals(RegisterState.Loading, awaitItem())
            assertEquals(RegisterState.Error("Email ya registrado"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, repository.registerCallCount)
    }

    @Test
    fun register_when_failure_without_message_emits_action_specific_fallback_error() = runTest {
        val repository = FakeAuthRepository(registerResult = Result.failure(RuntimeException()))
        val viewModel = buildViewModel(repository)

        viewModel.registerState.test {
            assertEquals(RegisterState.Idle, awaitItem())
            viewModel.register(sampleRegisterRequest())
            assertEquals(RegisterState.Loading, awaitItem())
            assertEquals(RegisterState.Error("Registro fallido"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, repository.registerCallCount)
    }

    @Test
    fun register_when_invalid_email_emits_validationerrors_and_does_not_call_repository() =
        runTest {
            val repository = FakeAuthRepository()
            val viewModel = buildViewModel(repository)

            viewModel.register(sampleRegisterRequest().copy(email = "invalid-email"))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.registerState.value
            assertTrue(state is RegisterState.ValidationErrors)
            assertTrue(state.fieldErrors.isNotEmpty())
            assertEquals(0, repository.registerCallCount)
        }

    @Test
    fun register_when_password_too_short_emits_validationerrors_and_does_not_call_repository() =
        runTest {
            val repository = FakeAuthRepository()
            val viewModel = buildViewModel(repository)

            viewModel.register(sampleRegisterRequest().copy(password = "123"))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.registerState.value
            assertTrue(state is RegisterState.ValidationErrors)
            assertTrue(state.fieldErrors.isNotEmpty())
            assertEquals(0, repository.registerCallCount)
        }

    @Test
    fun resetstates_after_register_success_restores_registerstate_to_idle() = runTest {
        val viewModel = buildViewModel()

        viewModel.register(sampleRegisterRequest())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.resetStates()

        assertEquals(RegisterState.Idle, viewModel.registerState.value)
    }

    // ─────────────────────────────────────────────────────────────
    // Reset password
    // ─────────────────────────────────────────────────────────────

    @Test
    fun resetpassword_when_success_emits_loading_then_success() = runTest {
        val viewModel = buildViewModel(
            FakeAuthRepository(resetPasswordResult = Result.success(Unit))
        )

        viewModel.isResettingPassword.test {
            assertEquals(ResetPasswordState.Idle, awaitItem())
            viewModel.resetPassword("mail@test.com")
            assertEquals(ResetPasswordState.Loading, awaitItem())
            assertEquals(
                ResetPasswordState.Success("Contraseña restablecida exitosamente"),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun resetpassword_when_failure_with_message_emits_error_with_that_message() =
        runTest {
            val viewModel = buildViewModel(
                FakeAuthRepository(
                    resetPasswordResult = Result.failure(RuntimeException("Email no encontrado"))
                )
            )

            viewModel.isResettingPassword.test {
                assertEquals(ResetPasswordState.Idle, awaitItem())
                viewModel.resetPassword("unknown@test.com")
                assertEquals(ResetPasswordState.Loading, awaitItem())
                assertEquals(ResetPasswordState.Error("Email no encontrado"), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun resetpassword_when_failure_without_message_emits_action_specific_fallback_error() =
        runTest {
            val viewModel = buildViewModel(
                FakeAuthRepository(resetPasswordResult = Result.failure(RuntimeException()))
            )

            viewModel.isResettingPassword.test {
                assertEquals(ResetPasswordState.Idle, awaitItem())
                viewModel.resetPassword("mail@test.com")
                assertEquals(ResetPasswordState.Loading, awaitItem())
                assertEquals(ResetPasswordState.Error("Error al restablecer"), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun resetresettingpasswordstate_after_success_restores_state_to_idle() =
        runTest {
            val viewModel = buildViewModel(
                FakeAuthRepository(resetPasswordResult = Result.success(Unit))
            )

            viewModel.isResettingPassword.test {
                assertEquals(ResetPasswordState.Idle, awaitItem())
                viewModel.resetPassword("mail@test.com")
                skipItems(2) // Loading + Success
                viewModel.resetResettingPasswordState()
                assertEquals(ResetPasswordState.Idle, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    // ─────────────────────────────────────────────────────────────
    // Change password
    // ─────────────────────────────────────────────────────────────

    @Test
    fun changepassword_when_success_emits_loading_then_success() = runTest {
        val viewModel = buildViewModel(
            FakeAuthRepository(changePasswordResult = Result.success(Unit))
        )

        viewModel.isChangingPassword.test {
            assertEquals(ChangePasswordState.Idle, awaitItem())
            viewModel.changePassword("Oldpass1", "Newpass1", "Newpass1", 1)
            assertEquals(ChangePasswordState.Loading, awaitItem())
            assertEquals(
                ChangePasswordState.Success("Contraseña cambiada exitosamente"),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun changepassword_when_failure_with_message_emits_error_with_that_message() =
        runTest {
            val viewModel = buildViewModel(
                FakeAuthRepository(
                    changePasswordResult = Result.failure(RuntimeException("Contraseña actual incorrecta"))
                )
            )

            viewModel.isChangingPassword.test {
                assertEquals(ChangePasswordState.Idle, awaitItem())
                viewModel.changePassword("WrongOld1", "Newpass1", "Newpass1", 1)
                assertEquals(ChangePasswordState.Loading, awaitItem())
                assertEquals(
                    ChangePasswordState.Error("Contraseña actual incorrecta"),
                    awaitItem()
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun changepassword_when_failure_without_message_emits_action_specific_fallback_error() =
        runTest {
            val viewModel = buildViewModel(
                FakeAuthRepository(changePasswordResult = Result.failure(RuntimeException()))
            )

            viewModel.isChangingPassword.test {
                assertEquals(ChangePasswordState.Idle, awaitItem())
                viewModel.changePassword("Oldpass1", "Newpass1", "Newpass1", 1)
                assertEquals(ChangePasswordState.Loading, awaitItem())
                assertEquals(ChangePasswordState.Error("Error al cambiar contraseña"), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun changepassword_when_new_passwords_do_not_match_emits_validationerror_and_does_not_call_repository() =
        runTest {
            val repository = FakeAuthRepository()
            val viewModel = buildViewModel(repository)

            viewModel.changePassword("Oldpass1", "Newpass1", "Different1", 1)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.isChangingPassword.value
            assertTrue(state is ChangePasswordState.Error)
            assertEquals(0, repository.loginCallCount) // repositorio no fue tocado
        }

    @Test
    fun resetchangepasswordstate_after_success_restores_state_to_idle() =
        runTest {
            val viewModel = buildViewModel(
                FakeAuthRepository(changePasswordResult = Result.success(Unit))
            )

            viewModel.isChangingPassword.test {
                assertEquals(ChangePasswordState.Idle, awaitItem())
                viewModel.changePassword("Oldpass1", "Newpass1", "Newpass1", 1)
                skipItems(2) // Loading + Success
                viewModel.resetChangePasswordState()
                assertEquals(ChangePasswordState.Idle, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    // ─────────────────────────────────────────────────────────────
    // Fixtures
    // ─────────────────────────────────────────────────────────────

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
            sex = "Male",
            dateOfBirth = "01011990",
            postCode = "28001",
            postAddress = "Street 1",
            dni = "12345678Z",
            deviceType = "android",
            profilePicBytes = null,
            profilePicName = null
        )
    }
}
