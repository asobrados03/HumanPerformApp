package com.humanperformcenter.shared.presentation.viewmodel

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.domain.usecase.UserAccountUseCase
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.presentation.ui.DeleteUserState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class UserSessionViewModel(
    private val userAccountUseCase: UserAccountUseCase,
    private val authUseCase: AuthUseCase,
    private val authLocalDataSource: AuthLocalDataSource,
) : ViewModel() {
    companion object {
        val log = logging()
    }

    val isLoggedInFlow: Flow<Boolean> = authLocalDataSource.accessTokenFlow()
        .map { token -> token.isNotBlank() }
        .distinctUntilChanged()

    private val _isLoggedIn = MutableStateFlow<Boolean?>(viewModelScope, null)
    @NativeCoroutinesState
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    private val _userData = MutableStateFlow<User?>(viewModelScope, null)
    @NativeCoroutinesState
    val userData: StateFlow<User?> = _userData.asStateFlow()

    private val _isLoading = MutableStateFlow(viewModelScope, true)
    @NativeCoroutinesState
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _deleteState = MutableStateFlow<DeleteUserState>(viewModelScope, DeleteUserState.Idle)
    @NativeCoroutinesState
    val deleteState: StateFlow<DeleteUserState> = _deleteState.asStateFlow()

    private val _isLoggingOut = MutableStateFlow(viewModelScope, false)
    @NativeCoroutinesState
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    init {
        viewModelScope.launch {
            val initialLoginState = runCatching {
                authLocalDataSource.getAccessToken()?.isNotBlank() == true
            }.getOrDefault(false)
            _isLoggedIn.value = initialLoginState

            authLocalDataSource.accessTokenFlow()
                .map { token -> token.isNotBlank() }
                .distinctUntilChanged()
                .collect { _isLoggedIn.value = it }
        }

        viewModelScope.launch {
            authLocalDataSource.userFlow().collect { storedUser ->
                _userData.value = storedUser
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(email: String) {
        viewModelScope.launch {
            _deleteState.value = DeleteUserState.Loading

            userAccountUseCase.deleteUser(email).fold(
                onSuccess = {
                    authLocalDataSource.clear()
                    delay(1000)
                    _deleteState.value = DeleteUserState.Success
                },
                onFailure = { throwable ->
                    _deleteState.value = when {
                        throwable.message?.contains("no encontrado", ignoreCase = true) == true ->
                            DeleteUserState.NotFound(email)
                        else ->
                            DeleteUserState.Error(throwable.message ?: "Error desconocido")
                    }
                }
            )
        }
    }

    fun resetDeleteState() {
        _deleteState.value = DeleteUserState.Idle
    }

    fun deleteStateKind(): String = when (_deleteState.value) {
        is DeleteUserState.Idle -> "idle"
        is DeleteUserState.Loading -> "loading"
        is DeleteUserState.Success -> "success"
        is DeleteUserState.NotFound -> "notFound"
        is DeleteUserState.Error -> "error"
    }

    fun deleteStateMessage(): String? = when (val state = _deleteState.value) {
        is DeleteUserState.NotFound -> "No se encontró la cuenta para ${state.email}."
        is DeleteUserState.Error -> state.message
        else -> null
    }

    fun logout(onSuccess: () -> Unit) {
        if (_isLoggingOut.value) return

        viewModelScope.launch {
            try {
                _isLoggingOut.value = true

                val logoutResult = authUseCase.logout()
                if (logoutResult.isFailure) {
                    log.debug {
                        "DEBUG: Logout remoto falló, pero la sesión local se limpió: ${logoutResult.exceptionOrNull()?.message}"
                    }
                }

                log.debug { "DEBUG: Sesión local eliminada desde AuthUseCase" }
                delay(800)
                _isLoggingOut.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoggingOut.value = false
                log.debug { "DEBUG: Error en logout: ${e.message}" }
                onSuccess()
            }
        }
    }

    fun currentUserState(): MutableStateFlow<User?> = _userData
}
