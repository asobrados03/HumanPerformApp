package com.humanperformcenter.ui.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.data.Session
import com.humanperformcenter.data.SessionRepository
import com.humanperformcenter.di.AppModule.userUseCase
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.domain.security.AuthPreferences.accessTokenFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SessionViewModel(
    private val repository: SessionRepository,
    prefs: DataStore<Preferences>
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

    fun setUserCredentials(token: String, id: Int) {
        _accessToken.value = token
        _userId.value = id
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

    val isLoggedInFlow: Flow<Boolean> = accessTokenFlow(prefs)
        .map { token -> token.isNotBlank() }
        .distinctUntilChanged()
}
