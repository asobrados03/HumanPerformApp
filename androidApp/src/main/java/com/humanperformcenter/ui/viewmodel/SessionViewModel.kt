package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.data.Session
import com.humanperformcenter.data.SessionRepository
import com.humanperformcenter.di.AppModule.userUseCase
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.domain.storage.SecureStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SessionViewModel(
    private val repository: SessionRepository
) : ViewModel() {

    val getAllSessions: Flow<List<Session>> = repository.getAllSessions()

    private val _entrenamientosContratados = MutableStateFlow(0)
    val entrenamientosContratados: StateFlow<Int> = _entrenamientosContratados

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> get() = _accessToken

    private val _allowedServices = MutableStateFlow<List<ServiceAvailable>>(emptyList())
    val allowedServices: StateFlow<List<ServiceAvailable>> get() = _allowedServices

    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> get() = _userId

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> get() = _userEmail

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> get() = _userName

    private val _userStreet = MutableStateFlow<String?>(null)
    val userStreet: StateFlow<String?> get() = _userStreet

    private val _userPostalCode = MutableStateFlow<Int?>(null)
    val userPostalCode: StateFlow<Int?> get() = _userPostalCode

    fun setUserCredentials(token: String, id: Int, email: String, nombre: String, calle: String, codigoPostal: Int?) {
        _accessToken.value = token
        _userId.value = id
        _userEmail.value = email
        _userName.value = nombre
        _userStreet.value = calle
        _userPostalCode.value = codigoPostal
    }

    fun insertSession(session: Session) {
        viewModelScope.launch {
            repository.insert(session)
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch {
            repository.delete(session)
        }
    }

    fun comprarEntrenamiento(sesionesPorSemana: Int) {
        _entrenamientosContratados.value = sesionesPorSemana
    }

    fun cargarServiciosPermitidos(userId: Int) {
        viewModelScope.launch {
            _allowedServices.value = userUseCase.getUserAllowedServices(userId)
        }
    }

    val isLoggedInFlow: Flow<Boolean> = SecureStorage.accessTokenFlow()
        .map { token -> token.isNotBlank() }
        .distinctUntilChanged()
}
