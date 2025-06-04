package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.LoginResponse
import com.humanperformcenter.shared.domain.usecase.UserUseCase
import com.humanperformcenter.shared.session.SessionManager
import com.humanperformcenter.ui.viewmodel.state.UpdateState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(
    private val userUseCase: UserUseCase
) : ViewModel() {

    // 1) Flujo en memoria que contiene el usuario logueado
    val userData: StateFlow<LoginResponse?> = SessionManager.user
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SessionManager.getCurrentUser()
        )

    // 2) LiveData para exponer el estado de la operación de update
    private val _updateState = MutableLiveData<UpdateState>(UpdateState.Idle)
    val updateState: LiveData<UpdateState> = _updateState

    /**
     * Llamar para actualizar el usuario en el backend.
     * Recibe un LoginResponse con los campos modificados (incluyendo id y token).
     * Actualiza SessionManager con el nuevo objeto si tiene éxito.
     */
    fun updateUser(updatedUser: LoginResponse) {
        _updateState.value = UpdateState.Loading

        viewModelScope.launch {
            val result = userUseCase.updateUser(updatedUser)

            result.onSuccess { newUser ->
                // 3) Almacenamos el usuario actualizado en memoria
                SessionManager.storeUser(newUser)
                _updateState.value = UpdateState.Success(newUser)
            }.onFailure { throwable ->
                _updateState.value = UpdateState.Error(throwable.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Opción para limpiar el estado luego de procesar el resultado en la UI.
     */
    fun clearUpdateState() {
        _updateState.value = UpdateState.Idle
    }
}
