package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.data.Session
import com.humanperformcenter.data.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionViewModel(private val repository: SessionRepository) : ViewModel() {

    val getAllSessions: Flow<List<Session>> = repository.getAllSessions()

    private val _entrenamientosContratados = MutableStateFlow(0)
    val entrenamientosContratados: StateFlow<Int> = _entrenamientosContratados


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

    fun isLoggedIn(): Boolean {
        // Revisa si hay sesión iniciada (ej. token en base de datos, preferencia guardada, etc.)
        return false
    }

    fun comprarEntrenamiento(sesionesPorSemana: Int) {
        _entrenamientosContratados.value = sesionesPorSemana
        // Puedes guardar también con DataStore si quieres persistencia
    }

}