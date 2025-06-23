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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class SessionViewModel(
    private val repository: SessionRepository,
    private val prefs: DataStore<Preferences>
) : ViewModel() {

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

    companion object {
        private val KEY_ACCESS = stringPreferencesKey("access_token_enc")
    }

    /** Flow que emite `true` si hay un token no–vacío */
    val isLoggedInFlow: Flow<Boolean> = prefs.data
        .map { it[KEY_ACCESS].orEmpty().isNotBlank() }
        .distinctUntilChanged()

    fun comprarEntrenamiento(sesionesPorSemana: Int) {
        _entrenamientosContratados.value = sesionesPorSemana
        // Puedes guardar también con DataStore si quieres persistencia
    }

}