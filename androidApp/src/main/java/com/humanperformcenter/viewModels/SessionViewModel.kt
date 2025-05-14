package com.humanperformcenter.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.data.Session
import com.humanperformcenter.data.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SessionViewModel(private val repository: SessionRepository) : ViewModel() {

    val getAllSessions: Flow<List<Session>> = repository.getAllSessions()

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
}