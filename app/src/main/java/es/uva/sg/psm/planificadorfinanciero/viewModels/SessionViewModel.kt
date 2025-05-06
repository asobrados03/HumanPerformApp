package es.uva.sg.psm.planificadorfinanciero.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uva.sg.psm.planificadorfinanciero.data.Session
import es.uva.sg.psm.planificadorfinanciero.data.SessionRepository
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
}