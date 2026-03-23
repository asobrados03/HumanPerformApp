package com.humanperformcenter.shared.presentation.viewmodel

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.domain.storage.SecureStorage
import com.humanperformcenter.shared.domain.usecase.AccountUseCase
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.presentation.ui.DeleteUserState
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class UserSessionViewModel(
    private val accountUseCase: AccountUseCase,
    private val authUseCase: AuthUseCase
) : ViewModel() {
    companion object {
        val log = logging()
    }

    val isLoggedInFlow: Flow<Boolean> = SecureStorage.accessTokenFlow()
        .map { token -> token.isNotBlank() }
        .distinctUntilChanged()

    private val _userData = MutableStateFlow<com.humanperformcenter.shared.data.model.user.User?>(null)
    @NativeCoroutinesState
    val userData: StateFlow<com.humanperformcenter.shared.data.model.user.User?> = _userData

    private val _isLoading = MutableStateFlow(true)
    @NativeCoroutinesState
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _deleteState = MutableStateFlow<DeleteUserState>(DeleteUserState.Idle)
    @NativeCoroutinesState
    val deleteState: StateFlow<DeleteUserState> = _deleteState

    private val _isLoggingOut = MutableStateFlow(false)
    @NativeCoroutinesState
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    init {
        viewModelScope.launch {
            SecureStorage.userFlow().collect { storedUser ->
                _userData.value = storedUser
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(email: String) {
        viewModelScope.launch {
            _deleteState.value = DeleteUserState.Loading

            accountUseCase.deleteUser(email).fold(
                onSuccess = {
                    SecureStorage.clear()
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

    fun currentUserState(): MutableStateFlow<com.humanperformcenter.shared.data.model.user.User?> = _userData
}
