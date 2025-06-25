package com.humanperformcenter.ui.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.data.Session
import com.humanperformcenter.data.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SessionViewModel(
    private val repository: SessionRepository,
    private val prefs: DataStore<Preferences>
) : ViewModel() {

    val getAllSessions: Flow<List<Session>> = repository.getAllSessions()

    private val _entrenamientosContratados = MutableStateFlow(0)
    val entrenamientosContratados: StateFlow<Int> = _entrenamientosContratados

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> get() = _accessToken

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

    companion object {
        private val KEY_ACCESS = stringPreferencesKey("access_token_enc")
    }

    val isLoggedInFlow: Flow<Boolean> = prefs.data
        .map { it[KEY_ACCESS].orEmpty().isNotBlank() }
        .distinctUntilChanged()
}
